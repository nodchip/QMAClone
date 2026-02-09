# WebSocket Transport Unification Design (DevMode, Tomcat, Production)

## Goal
Keep two runtime implementations:
- DevMode: Jetty WebSocketServlet
- Tomcat/Production: JSR-356 @ServerEndpoint

Unify three layers:
1. Connection lifecycle handling (join/bye/send)
2. Route and URL/path resolution
3. Operational logging format for troubleshooting

## Problems in current state
- Environment-specific logic is duplicated across servlet/endpoint classes.
- URL/path definitions are split, causing mismatch (404/500 risk).
- Logs are not normalized, so incident triage is slow.

## Architecture
### 1. WebSocketTransportFacade
Introduce a shared facade and move runtime-independent behavior into it.

Main APIs:
- handleOpen(route, sessionAdapter)
- handleClose(route, sessionAdapter, reason)
- handleError(route, sessionAdapter, throwable)

Servlets/endpoints become thin adapters that only forward events.

### 2. Session abstraction
Introduce `WebSocketSessionAdapter` to hide Jetty vs JSR-356 differences.

Interface:
- getId()
- getRemoteAddress()
- sendText(String)
- close()

Implementations:
- JettySessionAdapter
- Jsr356SessionAdapter

### 3. Single source for route definitions
Introduce `WebSocketRouteRegistry`.

Each route definition contains:
- packetType
- serverPath (/websocket/...)
- devModePath (/devmode-websocket/...)
- senderResolver (resolve MessageSender)

Both server registration and client URL generation are derived from this registry.

### 4. Runtime mode resolver
Introduce `ServerRuntimeModeResolver`.

Modes:
- DEV_JETTY
- TOMCAT_LOCAL
- TOMCAT_PROD

Use this mode in branching and structured logs.

## Data flow
1. Client connects to route URL.
2. Servlet/endpoint receives event.
3. Convert runtime session into WebSocketSessionAdapter.
4. Delegate to WebSocketTransportFacade.handleOpen().
5. Resolve MessageSender from route and call join().
6. On close/error, call handleClose()/handleError() and call bye().

## Error handling
Normalize errors into standard codes:
- ROUTE_NOT_FOUND
- SESSION_INVALID
- SENDER_RESOLVE_FAILED
- SEND_FAILED
- RUNTIME_CLASSLOADER_ERROR

Always log normalized error code with context.

## Logging standard
Use fixed keys for all websocket events:
- event (open/close/error/send)
- route
- packetType
- userCode
- resolvedUrl
- runtimeMode
- errorCode
- errorType

## Test plan
### Unit tests
- WebSocketRouteRegistry route derivation
- WebSocketTransportFacade lifecycle behavior
- WebSocketContextExtractor id/context extraction

### Component tests
- Jetty servlet adapter and JSR-356 adapter produce equivalent facade calls.

### Runtime verification
- DevMode: http://127.0.0.1:8888/QMAClone.html
- Tomcat local and production

Check:
- Successful websocket handshake
- Correct route mapping
- Standardized error log keys
- RPC fallback still works

## Done criteria
- Same route works in DevMode, Tomcat local, and production.
- Error logs always contain standard keys.
- URL/path definitions are centralized in WebSocketRouteRegistry.

## Non-goals
- Adding new websocket packet features
- Replacing web server platform