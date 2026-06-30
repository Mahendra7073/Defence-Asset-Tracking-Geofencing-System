<?xml version="1.0" encoding="UTF-8"?>
<StyledLayerDescriptor version="1.0.0"
    xmlns="http://www.opengis.net/sld"
    xmlns:ogc="http://www.opengis.net/ogc"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.opengis.net/sld
        http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd">
  <NamedLayer>
    <Name>geofence_zones</Name>
    <UserStyle>
      <Title>Geofence Zone Polygons</Title>
      <Abstract>Color-coded geofence zones by zone type</Abstract>
      <FeatureTypeStyle>
        <!-- Restricted zones: Red fill -->
        <Rule>
          <Name>Restricted</Name>
          <Title>Restricted Zone</Title>
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>zone_type</ogc:PropertyName>
              <ogc:Literal>restricted</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <PolygonSymbolizer>
            <Fill><CssParameter name="fill">#ff5252</CssParameter><CssParameter name="fill-opacity">0.25</CssParameter></Fill>
            <Stroke><CssParameter name="stroke">#ff5252</CssParameter><CssParameter name="stroke-width">2</CssParameter><CssParameter name="stroke-opacity">0.8</CssParameter></Stroke>
          </PolygonSymbolizer>
        </Rule>
        <!-- Safe zones: Green fill -->
        <Rule>
          <Name>Safe</Name>
          <Title>Safe Zone</Title>
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>zone_type</ogc:PropertyName>
              <ogc:Literal>safe</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <PolygonSymbolizer>
            <Fill><CssParameter name="fill">#00e676</CssParameter><CssParameter name="fill-opacity">0.2</CssParameter></Fill>
            <Stroke><CssParameter name="stroke">#00e676</CssParameter><CssParameter name="stroke-width">2</CssParameter><CssParameter name="stroke-opacity">0.8</CssParameter></Stroke>
          </PolygonSymbolizer>
        </Rule>
        <!-- Warning zones: Orange fill -->
        <Rule>
          <Name>Warning</Name>
          <Title>Warning Zone</Title>
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>zone_type</ogc:PropertyName>
              <ogc:Literal>warning</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <PolygonSymbolizer>
            <Fill><CssParameter name="fill">#ffab40</CssParameter><CssParameter name="fill-opacity">0.2</CssParameter></Fill>
            <Stroke><CssParameter name="stroke">#ffab40</CssParameter><CssParameter name="stroke-width">2</CssParameter><CssParameter name="stroke-opacity">0.8</CssParameter><CssParameter name="stroke-dasharray">6 3</CssParameter></Stroke>
          </PolygonSymbolizer>
        </Rule>
        <!-- Monitored zones: Blue fill -->
        <Rule>
          <Name>Monitored</Name>
          <Title>Monitored Zone</Title>
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>zone_type</ogc:PropertyName>
              <ogc:Literal>monitored</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <PolygonSymbolizer>
            <Fill><CssParameter name="fill">#448aff</CssParameter><CssParameter name="fill-opacity">0.15</CssParameter></Fill>
            <Stroke><CssParameter name="stroke">#448aff</CssParameter><CssParameter name="stroke-width">1.5</CssParameter><CssParameter name="stroke-opacity">0.7</CssParameter><CssParameter name="stroke-dasharray">4 4</CssParameter></Stroke>
          </PolygonSymbolizer>
        </Rule>
      </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>
