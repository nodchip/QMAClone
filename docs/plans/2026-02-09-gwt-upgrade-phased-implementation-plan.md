# GWT + Related Libraries Upgrade (Phased Plan)

## Goal
- Upgrade GWT and related libraries in QMAClone in small safe steps.
- Keep both environments working: Eclipse DevMode and Tomcat 9 (Windows / Ubuntu).
- Make rollback easy by committing in small units.

## Confirmed Environment
- Primary Eclipse: `C:\Users\nodchip\eclipse\jee-2025-12\eclipse`
- Old Eclipse: `C:\home\application\eclipse` (reference only)
- JDK (dev/prod): Java 25
- Maven compiler: `source=25`, `target=25`
- Runtime style: keep dual path (DevMode + Tomcat)

## When to Switch to New Eclipse
Move all daily work to new Eclipse immediately when one of these appears:
1. `GWT SDK not installed.`
2. `More than one gwt-user.jar has been found on the classpath`
3. Classpath issues reproduced only on old Eclipse.

## Phase Plan

### Phase 1: Baseline Snapshot
1. Save current dependency tree.
2. Record current build/test/GWT compile status.
3. Record current runtime behavior on DevMode and Tomcat.

### Phase 2: Low-Risk Library Updates
1. Update low-risk libraries one-by-one (`gin`, `piriti`, `gwt-dnd`, `gwt-websockets`).
2. Verify after each update.
3. Commit each update separately.

### Phase 3: GWT Core Upgrade
1. Upgrade `gwt-user`, `gwt-dev`, `gwt-servlet` together.
2. Set compiler level to Java 25.
3. Verify build/test/GWT compile and DevMode runtime.

### Phase 4: High-Risk Libraries (Only If Needed)
1. Evaluate `gwt-incubator`, `gwt-plus`, `gwt-visualization` one by one.
2. If API mismatch appears, design replacement before applying.
3. Revert quickly if unstable.

### Phase 5: Final Verification
1. Validate on DevMode and Tomcat (Windows/Ubuntu).
2. Confirm screen rendering, RPC, and WebSocket handshake (101).
3. Save final report under `docs/plans/`.

## Concrete Command Runbook

### Phase 1 Commands
```powershell
mvn -q -DskipTests dependency:tree > target/dependency-tree-before.txt
mvn clean compile
mvn test
mvn -DskipTests gwt:compile
```

### Phase 2 Commands (repeat per library update)
```powershell
mvn -DskipTests compile
mvn -DskipTests gwt:compile
mvn -Dtest=ConstantTest test
```

### Phase 3 Commands
```powershell
mvn clean compile
mvn test
mvn -DskipTests gwt:compile
```

### Phase 4 Commands (per candidate)
```powershell
mvn -DskipTests compile
mvn -DskipTests gwt:compile
mvn test
```

### Phase 5 Commands
```powershell
mvn clean compile
mvn test
mvn -DskipTests gwt:compile
```

## Runtime Verification Checklist
- DevMode URL: `http://127.0.0.1:8888/QMAClone.html`
- Tomcat URL (Windows): local deployed URL
- Tomcat URL (Ubuntu): production/staging URL
- Check:
  - Main screen renders
  - Tab contents render
  - RPC requests succeed
  - WebSocket returns HTTP `101 Switching Protocols`

## Rollback Policy
- Roll back by smallest commit unit (one library update).
- For risky changes, isolate first and then apply.
- Use `git revert <commit>` instead of force reset.

## Done Criteria
- `mvn clean compile` succeeds
- `mvn test` succeeds
- `mvn -DskipTests gwt:compile` succeeds
- DevMode and Tomcat both pass runtime checklist
