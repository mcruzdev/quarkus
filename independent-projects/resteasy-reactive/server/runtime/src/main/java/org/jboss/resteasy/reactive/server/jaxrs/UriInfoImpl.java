package org.jboss.resteasy.reactive.server.jaxrs;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.resteasy.reactive.common.jaxrs.UriBuilderImpl;
import org.jboss.resteasy.reactive.common.util.PathSegmentImpl;
import org.jboss.resteasy.reactive.common.util.QuarkusMultivaluedHashMap;
import org.jboss.resteasy.reactive.common.util.URIDecoder;
import org.jboss.resteasy.reactive.common.util.UnmodifiableMultivaluedMap;
import org.jboss.resteasy.reactive.server.core.Deployment;
import org.jboss.resteasy.reactive.server.core.ResteasyReactiveRequestContext;
import org.jboss.resteasy.reactive.server.core.UriMatch;
import org.jboss.resteasy.reactive.server.mapping.RuntimeResource;
import org.jboss.resteasy.reactive.server.spi.ServerHttpRequest;

/**
 * UriInfo implementation
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
public class UriInfoImpl implements UriInfo {

    private final ResteasyReactiveRequestContext currentRequest;
    private MultivaluedMap<String, String> queryParams;

    // marker for which target the pathParams where created, may be null when getPathParams was never called
    private RuntimeResource pathParamsTargetMarker;
    private MultivaluedMap<String, String> pathParams;

    private URI requestUri;

    public UriInfoImpl(ResteasyReactiveRequestContext currentRequest) {
        this.currentRequest = currentRequest;
    }

    @Override
    public String getPath() {
        return getPath(true);
    }

    @Override
    public String getPath(boolean decode) {
        if (!decode)
            throw encodedNotSupported();
        // TCK says normalized
        String path = URIDecoder.decodeURIComponent(currentRequest.getPath(), false);
        // the path must not contain the prefix
        String prefix = currentRequest.getDeployment().getPrefix();
        if (prefix.isEmpty())
            return path;
        // else skip the prefix
        if (path.length() == prefix.length()) {
            return "/";
        }
        return path.substring(prefix.length());
    }

    @Override
    public List<PathSegment> getPathSegments() {
        return getPathSegments(true);
    }

    @Override
    public List<PathSegment> getPathSegments(boolean decode) {
        if (!decode)
            throw encodedNotSupported();
        return PathSegmentImpl.parseSegments(getPath(), decode);
    }

    @Override
    public URI getRequestUri() {
        if (requestUri == null) {
            ServerHttpRequest request = currentRequest.serverRequest();
            try {
                // TCK says normalized
                requestUri = new URI(currentRequest.getAbsoluteURI())
                        .normalize();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return requestUri;
    }

    @Override
    public UriBuilder getRequestUriBuilder() {
        return UriBuilder.fromUri(getRequestUri());
    }

    @Override
    public URI getAbsolutePath() {
        try {
            // TCK says normalized
            String effectiveURI = currentRequest.getAbsoluteURI();
            int queryParamsIndex = effectiveURI.indexOf('?');
            if (queryParamsIndex > 0) {
                // the spec says that getAbsolutePath() does not contain query parameters
                effectiveURI = effectiveURI.substring(0, queryParamsIndex);
            }
            return new URI(effectiveURI).normalize();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UriBuilder getAbsolutePathBuilder() {
        return UriBuilder.fromUri(getAbsolutePath());
    }

    @Override
    public URI getBaseUri() {
        try {
            Deployment deployment = currentRequest.getDeployment();
            // the TCK doesn't tell us, but Stuart and Georgios prefer dressing their base URIs with useless slashes ;)
            String prefix = "/";
            if (deployment != null) {
                // prefix can be empty, but if it's not it will not end with a slash
                prefix = deployment.getPrefix();
                if (prefix.isEmpty())
                    prefix = "/";
                else
                    prefix = prefix + "/";
            }
            return new URI(currentRequest.getScheme(), currentRequest.getAuthority(),
                    prefix,
                    null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UriBuilder getBaseUriBuilder() {
        return UriBuilder.fromUri(getBaseUri());
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters() {
        return getPathParameters(true);
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters(boolean decode) {
        if (!decode)
            throw encodedNotSupported();
        // pathParams have to be recreated when the target changes.
        // this happens e.g. when the ResteasyReactiveRequestContext#restart is called for sub resources
        // The sub resource, can have additional path params that are not present on the locator
        if (pathParams == null && pathParamsTargetMarker == null || pathParamsTargetMarker != currentRequest.getTarget()) {
            pathParams = currentRequest.getAllPathParameters(false);
            pathParamsTargetMarker = currentRequest.getTarget();
        }
        return new UnmodifiableMultivaluedMap<>(pathParams);
    }

    private RuntimeException encodedNotSupported() {
        return new IllegalArgumentException("We do not support non-decoded parameters");
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        return getQueryParameters(true);
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        if (!decode)
            throw encodedNotSupported();
        if (queryParams == null) {
            queryParams = new QuarkusMultivaluedHashMap<>();
            Collection<String> entries = currentRequest.serverRequest().queryParamNames();
            for (String i : entries) {
                queryParams.addAll(i, currentRequest.serverRequest().getAllQueryParams(i));
            }
        }
        return new UnmodifiableMultivaluedMap<>(queryParams);
    }

    @Override
    public List<String> getMatchedURIs() {
        return getMatchedURIs(true);
    }

    @Override
    public List<String> getMatchedURIs(boolean decode) {
        if (!decode)
            throw encodedNotSupported();
        if (currentRequest.getTarget() == null) {
            return Collections.emptyList();
        }
        List<UriMatch> oldMatches = currentRequest.getMatchedURIs();
        List<String> matched = new ArrayList<>();
        String last = null;

        for (int i = 0; i < oldMatches.size(); ++i) {
            String m = oldMatches.get(i).matched;
            if (!m.equals(last)) {
                matched.add(m);
                last = m;
            }
        }
        return matched;
    }

    @Override
    public List<Object> getMatchedResources() {
        List<UriMatch> oldMatches = currentRequest.getMatchedURIs();
        List<Object> matched = new ArrayList<>();
        for (int i = 0; i < oldMatches.size(); ++i) {
            Object target = oldMatches.get(i).target;
            if (target != null) {
                matched.add(target);
            }
        }
        return matched;
    }

    @Override
    public URI resolve(URI uri) {
        return getBaseUri().resolve(uri);
    }

    @Override
    public URI relativize(URI uri) {
        URI from = getRequestUri();
        URI to = uri;
        if (uri.getScheme() == null && uri.getHost() == null) {
            to = getBaseUriBuilder().replaceQuery(null).path(uri.getPath()).replaceQuery(uri.getQuery())
                    .fragment(uri.getFragment()).build();
        }
        return UriBuilderImpl.relativize(from, to);
    }
}
