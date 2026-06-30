# Branch Strategy

## Branch Model

This project follows a simplified **Git Flow** model.

```
main          ←── Stable, deployable releases
  │
  └── develop ←── Integration branch (all features merge here)
        │
        ├── feature/backend-login-servlet
        ├── feature/frontend-dashboard-cards
        ├── feature/gis-wms-layer-setup
        └── bugfix/db-alert-geometry-fix
```

---

## Branches

| Branch     | Purpose                          | Merges Into | Protected |
| ---------- | -------------------------------- | ----------- | --------- |
| `main`     | Production-ready releases        | —           | ✅ Yes    |
| `develop`  | Integration of completed features| `main`      | ✅ Yes    |
| `feature/*`| Individual feature development   | `develop`   | No        |
| `bugfix/*` | Bug fixes                        | `develop`   | No        |
| `hotfix/*` | Critical production fixes        | `main`      | No        |

---

## Rules

1. **Never push directly** to `main` or `develop`.
2. All changes go through **Pull Requests**.
3. At least **1 code review** required before merge.
4. Feature branches should be **short-lived** (1–3 days max).
5. Delete branches after merging.
6. Keep commits **atomic and descriptive**.

---

## Merge Flow

```
Developer creates feature/backend-login-servlet
  → Pushes to remote
  → Creates PR to develop
  → Code review by Project Lead
  → Merge to develop

When sprint complete:
  → Create PR from develop → main
  → Tag release: v1.0.0
```

---

## Release Tagging

```
v1.0.0 — Initial MVP release
v1.1.0 — Feature additions
v1.0.1 — Bug fix patch
```
