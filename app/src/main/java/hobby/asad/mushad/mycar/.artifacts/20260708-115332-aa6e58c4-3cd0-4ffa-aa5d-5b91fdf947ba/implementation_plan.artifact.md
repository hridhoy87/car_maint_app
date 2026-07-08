# Implementation Plan - Revert Route Activity & Finalize Drawer Reorganization

Revert the `RouteActivity` to its original Google Maps-based implementation (using `route_map.html`) and finalize the previously requested drawer menu reorganization and theme updates.

## Proposed Changes

### Route Activity (REVERT)

#### [RouteActivity.java](file:///D:/07.MyCar/app/src/main/java/hobby/asad/mushad/mycar/RouteActivity.java)
- Change the URL in `setupWebView()` from `leaflet_map.html` back to `route_map.html`.
- Update `updateMapLocation()` to call the `updateLocation()` JavaScript function (which redirects to Google Maps) instead of `updateMap()`.
- Pass the current theme and station type correctly to the web view.

### Drawer & Theme (PRESERVE)

- **[strings.xml](file:///D:/07.MyCar/app/src/main/res/values/strings.xml)**: Keep the updated menu strings.
- **[drawer_menu.xml](file:///D:/07.MyCar/app/src/main/res/menu/drawer_menu.xml)**: Preserve the reorganized structure (Groups a-l).
- **[colors.xml](file:///D:/07.MyCar/app/src/main/res/values/colors.xml)** & **[colors.xml (night)](file:///D:/07.MyCar/app/src/main/res/values-night/colors.xml)**: Keep `drawerHeaderBackground` for subtle header/body difference.
- **[nav_header_main.xml](file:///D:/07.MyCar/app/src/main/res/layout/nav_header_main.xml)**: Keep the background set to `@color/drawerHeaderBackground`.
- **[BaseActivity.java](file:///D:/07.MyCar/app/src/main/java/hobby/asad/mushad/mycar/BaseActivity.java)**: Preserve the updated `setupNavigation()` logic for the new drawer items.

### Cleanup

- **[DELETE] leaflet_map.html**: Remove the unused asset file if it exists (need to verify path).

## Verification Plan

### Manual Verification
1. **Route Activity**: Open "Route" and verify it loads the Google Maps search interface centered on current location (or showing the branding overlay first).
2. **Station Type**: Change the station type in the spinner and verify it triggers a new search on the map.
3. **Drawer Structure**: Re-verify the drawer items follow the new structure (a-d, line, e-h, line, i-l).
4. **Header Theme**: Re-verify the subtle grey background for the drawer header in light theme.
