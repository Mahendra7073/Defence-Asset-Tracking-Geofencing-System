# Frontend Development Roadmap

## Phase 1: Foundation (Days 1–3)
- [x] Create folder structure (`assets/css`, `assets/js`, `pages/`, `images/`)
- [x] Build CSS design system (variables, base, layout, components)
- [x] Create `index.html` landing page
- [x] Create `login.html` with styled form
- [x] Create `dashboard.html` with layout shell
- [x] Create `tracking.html` with Leaflet container
- [x] Create `geofence.html` with map + table layout
- [x] Create `alerts.html` with alert feed table
- [x] Create JS stubs: `app.js`, `auth.js`, `map.js`, `utils.js`

## Phase 2: Map Integration (Days 4–7)
- [ ] Initialize Leaflet map on `tracking.html` with OpenStreetMap tiles
- [ ] Add asset markers from `/api/positions/latest` GeoJSON
- [ ] Add geofence polygon overlays from `/api/geofences` GeoJSON
- [ ] Add GeoServer WMS tile layer as toggleable overlay
- [ ] Add zoom, scale, layer control widgets
- [ ] Implement custom marker icons per asset type

## Phase 3: Dashboard (Days 8–10)
- [ ] Connect KPI stat cards to `/api/dashboard` endpoint
- [ ] Initialize mini-map on dashboard with latest positions
- [ ] Render recent alerts table from `/api/alerts?limit=10`
- [ ] Add Chart.js for alert trends / asset activity charts

## Phase 4: CRUD Pages (Days 11–16)
- [ ] Asset management page with DataTables (add/edit/delete modals)
- [ ] Geofence creation with Leaflet.Draw polygon tool
- [ ] Geofence editing — select polygon, modify, save
- [ ] Alert acknowledge button with PUT API call
- [ ] Alert severity filter dropdown

## Phase 5: Route History (Days 17–20)
- [ ] Route history page with date range picker
- [ ] Render LineString path on map from `/api/tracks`
- [ ] Add path animation / time slider replay
- [ ] Display distance, avg speed, duration stats

## Phase 6: Polish (Days 21–24)
- [ ] Responsive sidebar (collapsible on mobile)
- [ ] Toast notifications for success/error actions
- [ ] Loading spinners during API calls
- [ ] Empty state messages for no-data scenarios
- [ ] Cross-browser testing (Chrome, Firefox, Edge)
