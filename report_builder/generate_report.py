import os
import sys
import matplotlib.pyplot as plt
import matplotlib.patches as patches
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import nsdecls, qn

def set_cell_background(cell, hex_color):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{hex_color}"/>')
    tcPr.append(shd)

def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = OxmlElement('w:tcMar')
    for m, val in [('w:top', top), ('w:bottom', bottom), ('w:left', left), ('w:right', right)]:
        node = OxmlElement(m)
        node.set(qn('w:w'), str(val))
        node.set(qn('w:type'), 'dxa')
        tcMar.append(node)
    tcPr.append(tcMar)

def add_heading_with_spacing(doc, text, level, before=18, after=6):
    heading = doc.add_heading(text, level=level)
    heading.paragraph_format.space_before = Pt(before)
    heading.paragraph_format.space_after = Pt(after)
    heading.paragraph_format.keep_with_next = True
    
    # Format text style
    run = heading.runs[0]
    run.font.name = 'Times New Roman'
    if level == 1:
        run.font.size = Pt(14)
        run.font.color.rgb = RGBColor(0, 0, 0)
        run.bold = True
    elif level == 2:
        run.font.size = Pt(12)
        run.font.color.rgb = RGBColor(50, 50, 50)
        run.bold = True
    elif level == 3:
        run.font.size = Pt(11)
        run.font.color.rgb = RGBColor(100, 100, 100)
        run.bold = True
        run.italic = True
    return heading

def add_paragraph_with_spacing(doc, text="", before=0, after=6, line_spacing=1.5, align=WD_ALIGN_PARAGRAPH.JUSTIFY):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(before)
    p.paragraph_format.space_after = Pt(after)
    p.paragraph_format.line_spacing = line_spacing
    p.alignment = align
    run = p.add_run(text)
    run.font.name = 'Times New Roman'
    run.font.size = Pt(12)
    return p

def add_code_block(doc, code_text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(6)
    p.paragraph_format.space_after = Pt(6)
    p.paragraph_format.line_spacing = 1.15
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    
    pBdr = parse_xml(r'<w:pBdr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">'
                     r'<w:left w:val="single" w:sz="24" w:space="12" w:color="A0A0A0"/>'
                     r'</w:pBdr>')
    p._p.get_or_add_pPr().append(pBdr)
    
    shd = parse_xml(r'<w:shd xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" w:fill="F5F5F5"/>')
    p._p.get_or_add_pPr().append(shd)
    
    run = p.add_run(code_text)
    run.font.name = 'Consolas'
    run.font.size = Pt(9.5)
    run.font.color.rgb = RGBColor(40, 40, 40)
    return p

def add_caption(doc, label, text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(4)
    p.paragraph_format.space_after = Pt(12)
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run(f"{label}: {text}")
    run.font.name = 'Times New Roman'
    run.font.size = Pt(10)
    run.italic = True
    run.bold = True
    return p

def generate_vector_diagrams(images_dir):
    os.makedirs(images_dir, exist_ok=True)
    plt.rcParams['font.sans-serif'] = 'Arial'
    plt.rcParams['font.family'] = 'sans-serif'
    
    # 1. Overall System Architecture
    fig, ax = plt.subplots(figsize=(8.5, 4.5), dpi=300)
    ax.axis('off')
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 5)
    ax.text(1.5, 3.8, "Leaflet.js Client\n(Web Frontend)", bbox=dict(boxstyle="round,pad=0.5", fc="#E2E8F0", ec="#4A5568", lw=1.5), ha='center', va='center', fontsize=9, weight='bold')
    ax.text(5.0, 3.8, "Apache Tomcat 9\n(Java 17 Servlet API)", bbox=dict(boxstyle="round,pad=0.5", fc="#E2E8F0", ec="#4A5568", lw=1.5), ha='center', va='center', fontsize=9, weight='bold')
    ax.text(5.0, 1.2, "GeoServer 2.25.2\n(WMS/WFS Renderer)", bbox=dict(boxstyle="round,pad=0.5", fc="#E2E8F0", ec="#4A5568", lw=1.5), ha='center', va='center', fontsize=9, weight='bold')
    ax.text(8.5, 2.5, "PostgreSQL 16\n+ PostGIS 3.4\n(Spatial Database)", bbox=dict(boxstyle="round,pad=0.5", fc="#EDF2F7", ec="#2B6CB0", lw=2), ha='center', va='center', fontsize=9, weight='bold')
    ax.annotate("", xy=(3.4, 3.8), xytext=(2.9, 3.8), arrowprops=dict(arrowstyle="<->", lw=1.5, color="#4A5568"))
    ax.text(3.15, 3.95, "REST JSON\n(HTTP 8080)", ha='center', va='center', fontsize=8)
    ax.annotate("", xy=(3.4, 1.5), xytext=(2.0, 3.2), arrowprops=dict(arrowstyle="->", lw=1.5, color="#4A5568"))
    ax.text(2.6, 2.1, "Map tiles WMS/WFS\n(HTTP 8085)", ha='center', va='center', rotation=34, fontsize=8)
    ax.annotate("", xy=(7.2, 2.8), xytext=(6.5, 3.5), arrowprops=dict(arrowstyle="->", lw=1.5, color="#2B6CB0"))
    ax.text(6.8, 3.3, "HikariCP\nJDBC Ingest", ha='center', va='center', fontsize=8)
    ax.annotate("", xy=(7.2, 2.2), xytext=(6.5, 1.5), arrowprops=dict(arrowstyle="->", lw=1.5, color="#2B6CB0"))
    ax.text(6.8, 1.7, "PostGIS Store\nQueries", ha='center', va='center', fontsize=8)
    plt.tight_layout()
    plt.savefig(os.path.join(images_dir, "diag_overall_arch.png"), dpi=300, bbox_inches='tight')
    plt.close()
    
    # 2. Docker Architecture Diagram
    fig, ax = plt.subplots(figsize=(8.5, 4.5), dpi=300)
    ax.axis('off')
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 5)
    rect_net = patches.Rectangle((0.2, 0.2), 9.6, 4.6, linewidth=1.5, edgecolor='#4A5568', linestyle='--', facecolor='#F8FAFC')
    ax.add_patch(rect_net)
    ax.text(0.5, 4.5, "Docker Bridge Network (defence-net)", fontsize=9, weight='bold', color='#4A5568')
    ax.text(2.0, 3.2, "tomcat container\n(defence-tomcat)\nBuild: Dockerfile\nPort: 8080:8080", bbox=dict(boxstyle="round,pad=0.5", fc="#E2E8F0", ec="#4A5568", lw=1.5), ha='center', va='center', fontsize=8, weight='bold')
    ax.text(2.0, 1.2, "geoserver container\n(defence-geoserver)\nImage: kartoza/geoserver\nPort: 8085:8080", bbox=dict(boxstyle="round,pad=0.5", fc="#E2E8F0", ec="#4A5568", lw=1.5), ha='center', va='center', fontsize=8, weight='bold')
    ax.text(5.5, 2.2, "postgres container\n(defence-postgres)\nImage: postgis/postgis\nPort: 5432:5432", bbox=dict(boxstyle="round,pad=0.5", fc="#EDF2F7", ec="#2B6CB0", lw=2), ha='center', va='center', fontsize=8, weight='bold')
    ax.text(8.5, 1.2, "geoserver-setup\n(defence-geoserver-setup)\nSidecar Helper\nExits (0) on complete", bbox=dict(boxstyle="round,pad=0.5", fc="#FEFCBF", ec="#D69E2E", lw=1.5), ha='center', va='center', fontsize=8, weight='bold')
    ax.annotate("", xy=(3.8, 2.2), xytext=(3.3, 2.9), arrowprops=dict(arrowstyle="->", lw=1.5, color="#2B6CB0"))
    ax.text(3.5, 2.65, "JDBC", ha='center', va='center', fontsize=8)
    ax.annotate("", xy=(3.8, 2.2), xytext=(3.3, 1.5), arrowprops=dict(arrowstyle="->", lw=1.5, color="#2B6CB0"))
    ax.text(3.5, 1.75, "PostGIS Link", ha='center', va='center', fontsize=8)
    ax.annotate("", xy=(3.4, 1.2), xytext=(7.1, 1.2), arrowprops=dict(arrowstyle="->", lw=1.5, color="#D69E2E"))
    ax.text(5.2, 0.95, "Auto-config REST API (WFS/WMS Setup)", ha='center', va='center', fontsize=8)
    
    ax.text(5.5, 4.2, "defence-pgdata volume\n(persistent PostgreSQL data)", bbox=dict(boxstyle="ellipse,pad=0.3", fc="#E2E8F0", ec="#4A5568", lw=1), ha='center', va='center', fontsize=7)
    ax.annotate("", xy=(5.5, 3.0), xytext=(5.5, 3.8), arrowprops=dict(arrowstyle="<->", lw=1, linestyle=":", color="#4A5568"))
    ax.text(8.5, 3.2, "defence-geoserver-data\n(persistent GeoServer styles)", bbox=dict(boxstyle="ellipse,pad=0.3", fc="#E2E8F0", ec="#4A5568", lw=1), ha='center', va='center', fontsize=7)
    ax.annotate("", xy=(3.2, 1.5), xytext=(7.1, 3.0), arrowprops=dict(arrowstyle="<->", lw=1, linestyle=":", color="#4A5568"))
    plt.tight_layout()
    plt.savefig(os.path.join(images_dir, "diag_docker_arch.png"), dpi=300, bbox_inches='tight')
    plt.close()

    # 3. Workflow Flowchart
    fig, ax = plt.subplots(figsize=(8.5, 5.0), dpi=300)
    ax.axis('off')
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 6)
    ax.text(5.0, 5.5, "1. Ingress: GPS Field Transceiver Ingests Coordinates", bbox=dict(boxstyle="round,pad=0.4", fc="#E2E8F0", ec="#4A5568", lw=1.5), ha='center', va='center', fontsize=8)
    ax.text(5.0, 4.4, "2. DB Save: Insert row to asset_positions table", bbox=dict(boxstyle="round,pad=0.4", fc="#E2E8F0", ec="#4A5568", lw=1.5), ha='center', va='center', fontsize=8)
    ax.text(5.0, 3.3, "3. Evaluation: Trigger invokes trigger_geofence_check()\nExecutes ST_Contains(geofence_polygon, asset_geom)", bbox=dict(boxstyle="round,pad=0.4", fc="#EDF2F7", ec="#2B6CB0", lw=2), ha='center', va='center', fontsize=8)
    ax.text(5.0, 2.1, "4. Breach\nDetected?", bbox=dict(boxstyle="darrow,pad=0.4", fc="#E2E8F0", ec="#4A5568", lw=1.5), ha='center', va='center', fontsize=8)
    ax.text(2.0, 1.0, "5A. Log Alarm: Insert CRITICAL\nalert inside alerts registry", bbox=dict(boxstyle="round,pad=0.4", fc="#FED7D7", ec="#9B2C2C", lw=1.5), ha='center', va='center', fontsize=8)
    ax.text(8.0, 1.0, "5B. Normal state: Update position\nwithout alert generation", bbox=dict(boxstyle="round,pad=0.4", fc="#C6F6D5", ec="#22543D", lw=1.5), ha='center', va='center', fontsize=8)
    ax.annotate("", xy=(5.0, 4.8), xytext=(5.0, 5.2), arrowprops=dict(arrowstyle="->", lw=1.5, color="#4A5568"))
    ax.annotate("", xy=(5.0, 3.7), xytext=(5.0, 4.1), arrowprops=dict(arrowstyle="->", lw=1.5, color="#4A5568"))
    ax.annotate("", xy=(5.0, 2.6), xytext=(5.0, 2.9), arrowprops=dict(arrowstyle="->", lw=1.5, color="#4A5568"))
    ax.annotate("", xy=(3.2, 1.0), xytext=(4.2, 2.1), arrowprops=dict(arrowstyle="->", lw=1.5, color="#9B2C2C"))
    ax.text(3.5, 1.7, "YES", ha='center', va='center', color="#9B2C2C", weight='bold')
    ax.annotate("", xy=(6.8, 1.0), xytext=(5.8, 2.1), arrowprops=dict(arrowstyle="->", lw=1.5, color="#22543D"))
    ax.text(6.5, 1.7, "NO", ha='center', va='center', color="#22543D", weight='bold')
    plt.tight_layout()
    plt.savefig(os.path.join(images_dir, "diag_workflow.png"), dpi=300, bbox_inches='tight')
    plt.close()

    # 4. Sequence Diagram
    fig, ax = plt.subplots(figsize=(8.5, 5.0), dpi=300)
    ax.axis('off')
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 6)
    lifelines = [
        ("Asset Transceiver", 1.5),
        ("PositionServlet", 3.5),
        ("PostGIS trigger", 5.5),
        ("Alerts Registry", 7.5),
        ("Leaflet Map UI", 9.5)
    ]
    for name, x in lifelines:
        ax.plot([x, x], [0.5, 5.2], color='#718096', linestyle='--', linewidth=1)
        ax.text(x, 5.4, name, ha='center', va='center', fontsize=8, weight='bold', bbox=dict(boxstyle="round,pad=0.3", fc="#E2E8F0", ec="#4A5568"))
    ax.annotate("", xy=(3.5, 4.5), xytext=(1.5, 4.5), arrowprops=dict(arrowstyle="->", lw=1.5, color="#4A5568"))
    ax.text(2.5, 4.7, "1. Post Coordinates\n(HTTP REST)", ha='center', va='center', fontsize=7)
    ax.annotate("", xy=(5.5, 3.8), xytext=(3.5, 3.8), arrowprops=dict(arrowstyle="->", lw=1.5, color="#2B6CB0"))
    ax.text(4.5, 4.0, "2. Save row (JDBC)", ha='center', va='center', fontsize=7)
    ax.annotate("", xy=(5.5, 3.0), xytext=(5.5, 3.4), arrowprops=dict(arrowstyle="->", lw=1.2, connectionstyle="arc3,rad=0.3", color="#2B6CB0"))
    ax.text(6.0, 3.2, "3. ST_Contains\nContainment Check", ha='left', va='center', fontsize=7)
    ax.annotate("", xy=(7.5, 2.5), xytext=(5.5, 2.5), arrowprops=dict(arrowstyle="->", lw=1.5, color="#9B2C2C"))
    ax.text(6.5, 2.7, "4. Create Alert\n(on breach)", ha='center', va='center', fontsize=7)
    ax.annotate("", xy=(3.5, 1.8), xytext=(9.5, 1.8), arrowprops=dict(arrowstyle="->", lw=1.5, color="#4A5568"))
    ax.text(6.5, 2.0, "5. Get positions & alarms (10s polling)", ha='center', va='center', fontsize=7)
    ax.annotate("", xy=(9.5, 1.0), xytext=(9.5, 1.4), arrowprops=dict(arrowstyle="->", lw=1.2, connectionstyle="arc3,rad=0.3", color="#4A5568"))
    ax.text(10.0, 1.2, "6. Redraw markers\n& trigger alarms", ha='left', va='center', fontsize=7)
    plt.tight_layout()
    plt.savefig(os.path.join(images_dir, "diag_sequence.png"), dpi=300, bbox_inches='tight')
    plt.close()

    # 5. Deployment Diagram
    fig, ax = plt.subplots(figsize=(8.5, 4.5), dpi=300)
    ax.axis('off')
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 5)
    ax.text(1.5, 2.5, "Client Workspace Node\n\n- Web Browser\n- Leaflet Mapping Engine\n- HTML5 Render Canvas\n- Session Cookie Jar", bbox=dict(boxstyle="round,pad=0.5", fc="#F7FAFC", ec="#4A5568", lw=1.5), ha='center', va='center', fontsize=8)
    rect_srv = patches.Rectangle((3.8, 0.5), 5.8, 4.0, linewidth=2, edgecolor='#1A365D', facecolor='#F7FAFC')
    ax.add_patch(rect_srv)
    ax.text(6.7, 4.2, "Enterprise Application Server VM", ha='center', va='center', fontsize=9, weight='bold', color='#1A365D')
    ax.text(5.0, 2.5, "Tomcat Webapp\nContainer\n(Port 8080)", bbox=dict(boxstyle="round,pad=0.4", fc="#E2E8F0", ec="#4A5568", lw=1), ha='center', va='center', fontsize=7)
    ax.text(8.2, 3.2, "PostgreSQL DB\nContainer\n(Port 5432)", bbox=dict(boxstyle="round,pad=0.4", fc="#EDF2F7", ec="#2B6CB0", lw=1.5), ha='center', va='center', fontsize=7)
    ax.text(8.2, 1.4, "GeoServer GIS\nContainer\n(Port 8085)", bbox=dict(boxstyle="round,pad=0.4", fc="#E2E8F0", ec="#4A5568", lw=1), ha='center', va='center', fontsize=7)
    ax.annotate("", xy=(7.3, 3.2), xytext=(5.9, 2.7), arrowprops=dict(arrowstyle="->", lw=1, color="#2B6CB0"))
    ax.text(6.6, 3.1, "Internal Port 5432", ha='center', rotation=20, fontsize=6)
    ax.annotate("", xy=(7.3, 1.6), xytext=(5.9, 2.3), arrowprops=dict(arrowstyle="->", lw=1, color="#4A5568"))
    ax.text(6.6, 1.8, "Internal Port 5432", ha='center', rotation=-20, fontsize=6)
    ax.annotate("", xy=(3.8, 2.5), xytext=(3.0, 2.5), arrowprops=dict(arrowstyle="<->", lw=1.5, color="#1A365D"))
    ax.text(3.4, 2.7, "HTTP", ha='center', fontsize=8)
    plt.tight_layout()
    plt.savefig(os.path.join(images_dir, "diag_deployment.png"), dpi=300, bbox_inches='tight')
    plt.close()

    # 6. Use Case Diagram
    fig, ax = plt.subplots(figsize=(8.5, 4.5), dpi=300)
    ax.axis('off')
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 5)
    ax.text(1.5, 4.0, "Field Asset\n(Transceiver)", ha='center', va='center', fontsize=8, weight='bold', bbox=dict(boxstyle="round", fc="#F7FAFC", ec="#4A5568"))
    ax.text(1.5, 2.5, "Command Operator\n(User)", ha='center', va='center', fontsize=8, weight='bold', bbox=dict(boxstyle="round", fc="#F7FAFC", ec="#4A5568"))
    ax.text(1.5, 1.0, "GIS Administrator\n(Sysadmin)", ha='center', va='center', fontsize=8, weight='bold', bbox=dict(boxstyle="round", fc="#F7FAFC", ec="#4A5568"))
    ax.text(6.0, 4.2, "Ingest GPS Telemetry", bbox=dict(boxstyle="ellipse,pad=0.3", fc="#E2E8F0", ec="#4A5568"), ha='center', fontsize=8)
    ax.text(6.0, 3.4, "Monitor Active Markers", bbox=dict(boxstyle="ellipse,pad=0.3", fc="#E2E8F0", ec="#4A5568"), ha='center', fontsize=8)
    ax.text(6.0, 2.6, "Manage Geofence Zones", bbox=dict(boxstyle="ellipse,pad=0.3", fc="#E2E8F0", ec="#4A5568"), ha='center', fontsize=8)
    ax.text(6.0, 1.8, "Acknowledge Breach Alerts", bbox=dict(boxstyle="ellipse,pad=0.3", fc="#E2E8F0", ec="#4A5568"), ha='center', fontsize=8)
    ax.text(6.0, 1.0, "Configure Spatial GIS Layers", bbox=dict(boxstyle="ellipse,pad=0.3", fc="#E2E8F0", ec="#4A5568"), ha='center', fontsize=8)
    ax.annotate("", xy=(4.5, 4.2), xytext=(2.2, 4.0), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.annotate("", xy=(4.5, 3.4), xytext=(2.2, 2.5), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.annotate("", xy=(4.5, 2.6), xytext=(2.2, 2.5), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.annotate("", xy=(4.5, 1.8), xytext=(2.2, 2.5), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.annotate("", xy=(4.5, 1.0), xytext=(2.2, 1.0), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    plt.tight_layout()
    plt.savefig(os.path.join(images_dir, "diag_use_case.png"), dpi=300, bbox_inches='tight')
    plt.close()

    # 7. Activity Diagram
    fig, ax = plt.subplots(figsize=(8.5, 5.0), dpi=300)
    ax.axis('off')
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 6)
    ax.text(5.0, 5.6, "START: Operator Login Authenticated", bbox=dict(boxstyle="round", fc="#E2E8F0"), ha='center', fontsize=8)
    ax.text(5.0, 4.6, "Load Map Dashboard & Load Spatial Layers", bbox=dict(boxstyle="round", fc="#E2E8F0"), ha='center', fontsize=8)
    ax.text(5.0, 3.6, "AJAX Telemetry Polling Loop Initiated (10s)", bbox=dict(boxstyle="round", fc="#E2E8F0"), ha='center', fontsize=8)
    ax.text(5.0, 2.6, "Redraw Leaflet Marker Canvas & Display Active Positions", bbox=dict(boxstyle="round", fc="#E2E8F0"), ha='center', fontsize=8)
    ax.text(5.0, 1.6, "Acknowledge Alerts in Alarms Log Grid", bbox=dict(boxstyle="round", fc="#E2E8F0"), ha='center', fontsize=8)
    ax.text(5.0, 0.6, "END: Generate Analytical Reports", bbox=dict(boxstyle="round", fc="#E2E8F0"), ha='center', fontsize=8)
    ax.annotate("", xy=(5.0, 5.0), xytext=(5.0, 5.4), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.annotate("", xy=(5.0, 4.0), xytext=(5.0, 4.4), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.annotate("", xy=(5.0, 3.0), xytext=(5.0, 3.4), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.annotate("", xy=(5.0, 2.0), xytext=(5.0, 2.4), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.annotate("", xy=(5.0, 1.0), xytext=(5.0, 1.4), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    plt.tight_layout()
    plt.savefig(os.path.join(images_dir, "diag_activity.png"), dpi=300, bbox_inches='tight')
    plt.close()

    # 8. GeoServer Request Flow
    fig, ax = plt.subplots(figsize=(8.5, 4.5), dpi=300)
    ax.axis('off')
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 5)
    ax.text(1.5, 2.5, "Browser Client\n(Leaflet Map)", bbox=dict(boxstyle="round,pad=0.4", fc="#E2E8F0", ec="#4A5568"), ha='center', va='center', fontsize=8)
    ax.text(5.0, 2.5, "GeoServer Engine\n(WMS/WFS Parser)", bbox=dict(boxstyle="round,pad=0.4", fc="#FEFCBF", ec="#D69E2E"), ha='center', va='center', fontsize=8)
    ax.text(8.5, 2.5, "PostgreSQL+PostGIS\n(Datastore)", bbox=dict(boxstyle="round,pad=0.4", fc="#EDF2F7", ec="#2B6CB0"), ha='center', va='center', fontsize=8)
    ax.annotate("", xy=(3.8, 2.7), xytext=(2.6, 2.7), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.text(3.2, 2.9, "1. GetMap\nWMS request", ha='center', fontsize=7)
    ax.annotate("", xy=(2.6, 2.3), xytext=(3.8, 2.3), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.text(3.2, 2.0, "4. Return PNG\nImage Tile", ha='center', fontsize=7)
    ax.annotate("", xy=(7.3, 2.7), xytext=(6.2, 2.7), arrowprops=dict(arrowstyle="->", lw=1.2, color="#2B6CB0"))
    ax.text(6.75, 2.9, "2. JDBC Spatial\nQuery", ha='center', fontsize=7)
    ax.annotate("", xy=(6.2, 2.3), xytext=(7.3, 2.3), arrowprops=dict(arrowstyle="->", lw=1.2, color="#2B6CB0"))
    ax.text(6.75, 2.0, "3. Vector Geometries\nRecordset", ha='center', fontsize=7)
    plt.tight_layout()
    plt.savefig(os.path.join(images_dir, "diag_geoserver_flow.png"), dpi=300, bbox_inches='tight')
    plt.close()

    # 9. REST API Flow
    fig, ax = plt.subplots(figsize=(8.5, 4.5), dpi=300)
    ax.axis('off')
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 5)
    ax.text(1.5, 2.5, "Asset GPS\nTransmitter", bbox=dict(boxstyle="round,pad=0.4", fc="#E2E8F0", ec="#4A5568"), ha='center', va='center', fontsize=8)
    ax.text(5.0, 2.5, "Tomcat Server\n(PositionServlet)", bbox=dict(boxstyle="round,pad=0.4", fc="#E2E8F0", ec="#4A5568"), ha='center', va='center', fontsize=8)
    ax.text(8.5, 2.5, "PostgreSQL Database\n(HikariCP Pool)", bbox=dict(boxstyle="round,pad=0.4", fc="#EDF2F7", ec="#2B6CB0"), ha='center', va='center', fontsize=8)
    ax.annotate("", xy=(3.8, 2.7), xytext=(2.6, 2.7), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.text(3.2, 2.9, "1. POST JSON\nCoordinates", ha='center', fontsize=7)
    ax.annotate("", xy=(2.6, 2.3), xytext=(3.8, 2.3), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.text(3.2, 2.0, "4. Return HTTP\n201 Created", ha='center', fontsize=7)
    ax.annotate("", xy=(7.3, 2.7), xytext=(6.2, 2.7), arrowprops=dict(arrowstyle="->", lw=1.2, color="#2B6CB0"))
    ax.text(6.75, 2.9, "2. SQL Insert\nJDBC Statement", ha='center', fontsize=7)
    ax.annotate("", xy=(6.2, 2.3), xytext=(7.3, 2.3), arrowprops=dict(arrowstyle="->", lw=1.2, color="#2B6CB0"))
    ax.text(6.75, 2.0, "3. Trigger Fired\nST_Contains check", ha='center', fontsize=7)
    plt.tight_layout()
    plt.savefig(os.path.join(images_dir, "diag_rest_flow.png"), dpi=300, bbox_inches='tight')
    plt.close()

    # 10. Component Diagram
    fig, ax = plt.subplots(figsize=(8.5, 4.5), dpi=300)
    ax.axis('off')
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 5)
    ax.text(1.8, 4.0, "Leaflet Map UI Component\n(frontend map.js)", bbox=dict(boxstyle="round", fc="#E2E8F0"), ha='center', fontsize=8)
    ax.text(1.8, 1.5, "Authentication Component\n(frontend auth.js)", bbox=dict(boxstyle="round", fc="#E2E8F0"), ha='center', fontsize=8)
    ax.text(5.0, 2.7, "API Gateway / Servlets Component\n(backend Java Servlets)", bbox=dict(boxstyle="round", fc="#FEFCBF"), ha='center', fontsize=8)
    ax.text(8.2, 4.0, "HikariCP JDBC Component\n(backend Database)", bbox=dict(boxstyle="round", fc="#EDF2F7"), ha='center', fontsize=8)
    ax.text(8.2, 1.5, "PostGIS Spatial Engine\n(PostgreSQL Spatial)", bbox=dict(boxstyle="round", fc="#EDF2F7"), ha='center', fontsize=8)
    
    ax.annotate("", xy=(3.5, 2.7), xytext=(3.0, 4.0), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.annotate("", xy=(3.5, 2.7), xytext=(3.0, 1.5), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.annotate("", xy=(6.5, 3.8), xytext=(5.5, 3.0), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.annotate("", xy=(6.5, 1.8), xytext=(5.5, 2.5), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    plt.tight_layout()
    plt.savefig(os.path.join(images_dir, "diag_component.png"), dpi=300, bbox_inches='tight')
    plt.close()

    # 11. Class Diagram
    fig, ax = plt.subplots(figsize=(8.5, 5.0), dpi=300)
    ax.axis('off')
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 6)
    # User Class
    ax.text(2.0, 4.8, "User\n------\n- username: String\n- role: String\n------\n+ authenticate()", bbox=dict(boxstyle="square", fc="#F8FAFC", ec="#4A5568"), ha='center', fontsize=7)
    # Asset Class
    ax.text(5.0, 4.8, "Asset\n------\n- assetCode: String\n- assetType: String\n------\n+ register()", bbox=dict(boxstyle="square", fc="#F8FAFC", ec="#4A5568"), ha='center', fontsize=7)
    # Position Class
    ax.text(8.0, 4.8, "Position\n------\n- geom: Point\n- recordedAt: Date\n------\n+ savePosition()", bbox=dict(boxstyle="square", fc="#F8FAFC", ec="#4A5568"), ha='center', fontsize=7)
    # Geofence Class
    ax.text(3.5, 2.0, "GeofenceZone\n------\n- zoneName: String\n- geom: Polygon\n------\n+ checkBreach()", bbox=dict(boxstyle="square", fc="#F8FAFC", ec="#4A5568"), ha='center', fontsize=7)
    # Alert Class
    ax.text(6.5, 2.0, "Alert\n------\n- severity: String\n- isAcked: boolean\n------\n+ triggerAlarm()", bbox=dict(boxstyle="square", fc="#F8FAFC", ec="#4A5568"), ha='center', fontsize=7)
    
    # Arrows
    ax.annotate("", xy=(5.0, 3.8), xytext=(5.0, 4.3), arrowprops=dict(arrowstyle="<-", lw=1.2, color="#4A5568"))
    ax.annotate("", xy=(3.5, 3.2), xytext=(4.5, 4.3), arrowprops=dict(arrowstyle="<-", lw=1.2, color="#4A5568"))
    ax.annotate("", xy=(6.5, 3.2), xytext=(5.5, 4.3), arrowprops=dict(arrowstyle="<-", lw=1.2, color="#4A5568"))
    plt.tight_layout()
    plt.savefig(os.path.join(images_dir, "diag_class.png"), dpi=300, bbox_inches='tight')
    plt.close()

    # 12. State Diagram
    fig, ax = plt.subplots(figsize=(8.5, 4.5), dpi=300)
    ax.axis('off')
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 5)
    ax.text(1.5, 2.5, "REGISTERED", bbox=dict(boxstyle="round", fc="#E2E8F0"), ha='center', fontsize=8)
    ax.text(4.5, 2.5, "ACTIVE_TRACKING", bbox=dict(boxstyle="round", fc="#FEFCBF"), ha='center', fontsize=8)
    ax.text(8.0, 4.0, "PERIMETER_BREACH\n(Alert State)", bbox=dict(boxstyle="round", fc="#FED7D7", ec="#9B2C2C"), ha='center', fontsize=8)
    ax.text(8.0, 1.0, "SOS_EMERGENCY\n(Critical State)", bbox=dict(boxstyle="round", fc="#FED7D7", ec="#9B2C2C", lw=2), ha='center', fontsize=8)
    # Transitions
    ax.annotate("", xy=(3.5, 2.5), xytext=(2.2, 2.5), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.text(2.8, 2.7, "Start Ingestion", ha='center', fontsize=7)
    ax.annotate("", xy=(7.0, 3.7), xytext=(5.5, 2.8), arrowprops=dict(arrowstyle="->", lw=1.2, color="#9B2C2C"))
    ax.text(6.0, 3.4, "Containment=True", ha='center', fontsize=7, color="#9B2C2C")
    ax.annotate("", xy=(7.0, 1.3), xytext=(5.5, 2.2), arrowprops=dict(arrowstyle="->", lw=1.2, color="#9B2C2C"))
    ax.text(6.0, 1.5, "Operator Panic", ha='center', fontsize=7, color="#9B2C2C")
    plt.tight_layout()
    plt.savefig(os.path.join(images_dir, "diag_state.png"), dpi=300, bbox_inches='tight')
    plt.close()

    # 13. Authentication Flow
    fig, ax = plt.subplots(figsize=(8.5, 4.5), dpi=300)
    ax.axis('off')
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 5)
    ax.text(1.5, 2.5, "User Inputs\nCredentials", bbox=dict(boxstyle="round", fc="#E2E8F0"), ha='center', fontsize=8)
    ax.text(4.5, 2.5, "AuthFilter.java\nIntercepts", bbox=dict(boxstyle="round", fc="#FEFCBF"), ha='center', fontsize=8)
    ax.text(7.5, 3.5, "BCrypt Match?\nVerify Hash", bbox=dict(boxstyle="round", fc="#EDF2F7"), ha='center', fontsize=8)
    ax.text(7.5, 1.5, "Success:\nRedirect Dashboard", bbox=dict(boxstyle="round", fc="#C6F6D5"), ha='center', fontsize=8)
    ax.annotate("", xy=(3.5, 2.5), xytext=(2.2, 2.5), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.annotate("", xy=(6.5, 3.2), xytext=(5.5, 2.8), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.annotate("", xy=(7.5, 2.0), xytext=(7.5, 3.0), arrowprops=dict(arrowstyle="->", lw=1.2, color="#22543D"))
    ax.text(7.7, 2.5, "YES", ha='center', color="#22543D", weight='bold', fontsize=8)
    plt.tight_layout()
    plt.savefig(os.path.join(images_dir, "diag_auth_flow.png"), dpi=300, bbox_inches='tight')
    plt.close()

    # 14. Database Flow
    fig, ax = plt.subplots(figsize=(8.5, 4.5), dpi=300)
    ax.axis('off')
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 5)
    ax.text(1.5, 2.5, "Insert telemetry\nto asset_positions", bbox=dict(boxstyle="round", fc="#E2E8F0"), ha='center', fontsize=8)
    ax.text(4.5, 2.5, "PostgreSQL Trigger\nFires on Insert", bbox=dict(boxstyle="round", fc="#FEFCBF"), ha='center', fontsize=8)
    ax.text(7.5, 2.5, "PostGIS ST_Contains\nContainment check", bbox=dict(boxstyle="round", fc="#EDF2F7"), ha='center', fontsize=8)
    ax.annotate("", xy=(3.5, 2.5), xytext=(2.2, 2.5), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.annotate("", xy=(6.5, 2.5), xytext=(5.5, 2.5), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    plt.tight_layout()
    plt.savefig(os.path.join(images_dir, "diag_db_flow.png"), dpi=300, bbox_inches='tight')
    plt.close()

    # 15. Container Diagram
    fig, ax = plt.subplots(figsize=(8.5, 4.5), dpi=300)
    ax.axis('off')
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 5)
    rect_compose = patches.Rectangle((0.2, 0.2), 9.6, 4.6, linewidth=1.5, edgecolor='#1A365D', facecolor='#F7FAFC')
    ax.add_patch(rect_compose)
    ax.text(0.5, 4.5, "Docker Compose (Deployment Boundary)", fontsize=9, weight='bold', color='#1A365D')
    ax.text(2.0, 2.5, "Apache Tomcat JRE17\n(defence-tomcat)", bbox=dict(boxstyle="round", fc="#E2E8F0"), ha='center', fontsize=8)
    ax.text(5.0, 2.5, "PostgreSQL + PostGIS\n(defence-postgres)", bbox=dict(boxstyle="round", fc="#EDF2F7"), ha='center', fontsize=8)
    ax.text(8.0, 2.5, "GeoServer 2.25\n(defence-geoserver)", bbox=dict(boxstyle="round", fc="#E2E8F0"), ha='center', fontsize=8)
    plt.tight_layout()
    plt.savefig(os.path.join(images_dir, "diag_container.png"), dpi=300, bbox_inches='tight')
    plt.close()

    # 16. Module Dependency
    fig, ax = plt.subplots(figsize=(8.5, 4.5), dpi=300)
    ax.axis('off')
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 5)
    ax.text(2.0, 2.5, "Dashboard UI\nModule", bbox=dict(boxstyle="round", fc="#E2E8F0"), ha='center', fontsize=8)
    ax.text(5.0, 4.0, "Auth Session\nModule", bbox=dict(boxstyle="round", fc="#E2E8F0"), ha='center', fontsize=8)
    ax.text(5.0, 1.0, "Reports\nCompiler", bbox=dict(boxstyle="round", fc="#E2E8F0"), ha='center', fontsize=8)
    ax.text(8.0, 2.5, "JDBC Data\nAccess DAO", bbox=dict(boxstyle="round", fc="#FEFCBF"), ha='center', fontsize=8)
    # Arrows
    ax.annotate("", xy=(4.2, 3.8), xytext=(2.8, 2.7), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.annotate("", xy=(4.2, 1.2), xytext=(2.8, 2.3), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.annotate("", xy=(7.2, 2.5), xytext=(5.8, 3.8), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.annotate("", xy=(7.2, 2.5), xytext=(5.8, 1.2), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    plt.tight_layout()
    plt.savefig(os.path.join(images_dir, "diag_module_dependency.png"), dpi=300, bbox_inches='tight')
    plt.close()

    # 17. Network Diagram
    fig, ax = plt.subplots(figsize=(8.5, 4.5), dpi=300)
    ax.axis('off')
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 5)
    ax.text(1.5, 2.5, "VM Virtual Bridge\n172.20.0.0/16", bbox=dict(boxstyle="round", fc="#F8FAFC", ec="#4A5568"), ha='center', fontsize=8)
    ax.text(5.0, 4.0, "defence-tomcat\nIP: 172.20.0.3", bbox=dict(boxstyle="round", fc="#E2E8F0"), ha='center', fontsize=8)
    ax.text(5.0, 2.5, "defence-postgres\nIP: 172.20.0.2", bbox=dict(boxstyle="round", fc="#EDF2F7"), ha='center', fontsize=8)
    ax.text(5.0, 1.0, "defence-geoserver\nIP: 172.20.0.4", bbox=dict(boxstyle="round", fc="#E2E8F0"), ha='center', fontsize=8)
    # Arrows
    ax.annotate("", xy=(4.0, 3.8), xytext=(2.5, 2.7), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.annotate("", xy=(4.0, 2.5), xytext=(2.5, 2.5), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.annotate("", xy=(4.0, 1.2), xytext=(2.5, 2.3), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    plt.tight_layout()
    plt.savefig(os.path.join(images_dir, "diag_network.png"), dpi=300, bbox_inches='tight')
    plt.close()

    # 18. UML Communication Diagram
    fig, ax = plt.subplots(figsize=(8.5, 4.5), dpi=300)
    ax.axis('off')
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 5)
    ax.text(1.5, 2.5, "Map View", bbox=dict(boxstyle="round", fc="#E2E8F0"), ha='center', fontsize=8)
    ax.text(5.0, 2.5, "Servlet Controller", bbox=dict(boxstyle="round", fc="#FEFCBF"), ha='center', fontsize=8)
    ax.text(8.5, 2.5, "Data Store DAO", bbox=dict(boxstyle="round", fc="#EDF2F7"), ha='center', fontsize=8)
    
    ax.annotate("", xy=(3.8, 2.5), xytext=(2.5, 2.5), arrowprops=dict(arrowstyle="->", lw=1.2, color="#4A5568"))
    ax.text(3.1, 2.7, "1. POST Coordinates", ha='center', fontsize=7)
    ax.annotate("", xy=(7.3, 2.5), xytext=(6.2, 2.5), arrowprops=dict(arrowstyle="->", lw=1.2, color="#2B6CB0"))
    ax.text(6.75, 2.7, "2. savePosition()", ha='center', fontsize=7)
    plt.tight_layout()
    plt.savefig(os.path.join(images_dir, "diag_communication.png"), dpi=300, bbox_inches='tight')
    plt.close()
    
    print("All 18 vector-quality diagrams generated successfully!")

def main():
    workspace_root = r"c:\Users\ASUS\DRDO Project\Defence-Asset-Tracking-Geofencing-System"
    print(f"Workspace root: {workspace_root}")
    
    generate_vector_diagrams(os.path.join(workspace_root, "docs", "images"))
    
    doc = Document()
    
    # Configure document margins
    for section in doc.sections:
        section.top_margin = Inches(1.0)
        section.bottom_margin = Inches(1.0)
        section.left_margin = Inches(1.0)
        section.right_margin = Inches(1.0)
        section.different_first_page_header_footer = True
        
        # Header setup
        header = section.header
        hp = header.paragraphs[0]
        hp.alignment = WD_ALIGN_PARAGRAPH.RIGHT
        hrun = hp.add_run("Defence Asset Tracking & Geofencing System | DRDO Internship Project")
        hrun.font.name = 'Times New Roman'
        hrun.font.size = Pt(8.5)
        hrun.font.color.rgb = RGBColor(128, 128, 128)
        
        # Footer setup
        footer = section.footer
        fp = footer.paragraphs[0]
        fp.alignment = WD_ALIGN_PARAGRAPH.RIGHT
        frun = fp.add_run("Defence Laboratory Jodhpur (DLJ)  |  Page ")
        frun.font.name = 'Times New Roman'
        frun.font.size = Pt(9)
        frun.font.color.rgb = RGBColor(128, 128, 128)
        
        fldSimple = OxmlElement('w:fldSimple')
        fldSimple.set(qn('w:instr'), 'PAGE')
        fp._p.append(fldSimple)

    # COVER PAGE
    p = add_paragraph_with_spacing(doc, "A PROJECT REPORT ON", before=36, after=12, align=WD_ALIGN_PARAGRAPH.CENTER)
    p.runs[0].bold = True
    p.runs[0].font.size = Pt(14)
    
    p = add_paragraph_with_spacing(doc, "DEFENCE ASSET TRACKING & GEOFENCING SYSTEM", before=12, after=24, align=WD_ALIGN_PARAGRAPH.CENTER)
    p.runs[0].bold = True
    p.runs[0].font.size = Pt(22)
    p.runs[0].font.color.rgb = RGBColor(26, 54, 93)
    
    p = add_paragraph_with_spacing(doc, "Real-Time Telemetry Tracking, Geographic Perimeter Security, and Alerts Ingestion using PostGIS & GeoServer Integration", before=6, after=24, align=WD_ALIGN_PARAGRAPH.CENTER)
    p.runs[0].italic = True
    p.runs[0].font.size = Pt(12)
    
    p = add_paragraph_with_spacing(doc, "Submitted in partial fulfillment of the requirements for the award of", before=24, after=12, align=WD_ALIGN_PARAGRAPH.CENTER)
    p.runs[0].font.size = Pt(11)
    
    p = add_paragraph_with_spacing(doc, "INTERNSHIP COMPLETION CERTIFICATE", before=6, after=36, align=WD_ALIGN_PARAGRAPH.CENTER)
    p.runs[0].bold = True
    p.runs[0].font.size = Pt(13)
    
    p = add_paragraph_with_spacing(doc, "Developed during the DRDO Internship Program (May – July 2026)\nBy a Collaborative Student Internship Team:", before=24, after=6, align=WD_ALIGN_PARAGRAPH.CENTER)
    p.runs[0].bold = True
    p.runs[0].font.size = Pt(12)
    
    team_members = [
        "Mahendra Gurjar (3rd Year, CSE)",
        "Priyadarshini Choudhary (3rd Year, IT)",
        "Shahina Parvin (3rd Year, CSE)",
        "Gaurav Deora (2nd Year, CSE)",
        "Omprakash (2nd Year, CSE)",
        "Chandrika Solanki (2nd Year, IT)",
        "Abhimanyu Singh Rajpurohit (2nd Year, IT)",
        "Pinku Daila (2nd Year, AIDS)",
        "Kulwant Singh Rathore (2nd Year, ECC)"
    ]
    for member in team_members:
        p = add_paragraph_with_spacing(doc, f"• {member}", before=0, after=2, align=WD_ALIGN_PARAGRAPH.CENTER)
        p.runs[0].font.size = Pt(11)
        
    p = add_paragraph_with_spacing(doc, "Under the Guidance of:", before=36, after=6, align=WD_ALIGN_PARAGRAPH.CENTER)
    p.runs[0].font.size = Pt(11)
    
    p = add_paragraph_with_spacing(doc, "Shri Shyam Lal\nScientist 'F' / Internship Mentor\nDefence Laboratory Jodhpur (DLJ)", before=0, after=36, align=WD_ALIGN_PARAGRAPH.CENTER)
    p.runs[0].bold = True
    p.runs[0].font.size = Pt(12)
    
    p = add_paragraph_with_spacing(doc, "DEFENCE LABORATORY JODHPUR (DLJ)\nDEFENCE RESEARCH & DEVELOPMENT ORGANISATION (DRDO)\nMINISTRY OF DEFENCE, GOVERNMENT OF INDIA\nJODHPUR, RAJASTHAN – 342011", before=24, after=0, align=WD_ALIGN_PARAGRAPH.CENTER)
    p.runs[0].bold = True
    p.runs[0].font.size = Pt(12)
    
    doc.add_page_break()

    # CERTIFICATE OF ORIGINALITY
    add_heading_with_spacing(doc, "DEFENCE LABORATORY JODHPUR (DLJ)", 1, before=24, after=12)
    p = add_paragraph_with_spacing(doc, "DEFENCE RESEARCH & DEVELOPMENT ORGANISATION (DRDO)\nJODHPUR, RAJASTHAN", before=12, after=12)
    p.runs[0].bold = True
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    
    p = add_paragraph_with_spacing(doc, "CERTIFICATE OF ORIGINALITY", before=36, after=18, align=WD_ALIGN_PARAGRAPH.CENTER)
    p.runs[0].bold = True
    p.runs[0].font.size = Pt(14)
    
    p = add_paragraph_with_spacing(doc, before=18, after=12)
    p.runs[0].text = (
        "This is to certify that the project report entitled \"Defence Asset Tracking & Geofencing System\" is "
        "a bona fide record of work carried out by the Student Internship Team under the supervision and guidance of "
        "Shri Shyam Lal, Scientist 'F' at Defence Laboratory Jodhpur (DLJ), Jodhpur, during the period from May 2026 "
        "to July 2026, as part of their internship program. The work presented is original and has not been submitted "
        "elsewhere for the award of any degree or diploma."
    )
    p = add_paragraph_with_spacing(doc, before=48, after=0)
    p.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    run = p.add_run("______________________________\nShri Shyam Lal\nScientist 'F' / Project Coordinator\nDefence Laboratory Jodhpur (DLJ)\nDRDO, Jodhpur")
    run.bold = True
    
    doc.add_page_break()

    # DECLARATION BY STUDENTS
    add_heading_with_spacing(doc, "DECLARATION BY STUDENTS", 1, before=24, after=12)
    p = add_paragraph_with_spacing(doc, before=12, after=12)
    p.runs[0].text = (
        "We, the members of the Student Internship Team, hereby declare that the project work presented in this "
        "report entitled \"Defence Asset Tracking & Geofencing System\" is our own work, carried out under the guidance "
        "of Shri Shyam Lal, Scientist 'F', Defence Laboratory Jodhpur (DLJ). We have fully acknowledged all sources of "
        "information and references utilized during this project."
    )
    p = add_paragraph_with_spacing(doc, before=12, after=6)
    p.runs[0].text = "Names and Signatures of Team Members:"
    p.runs[0].bold = True
    for member in team_members:
        p = add_paragraph_with_spacing(doc, before=12, after=2)
        p.runs[0].text = f"_____________________________\t\t{member}"
        
    doc.add_page_break()

    # ACKNOWLEDGEMENT
    add_heading_with_spacing(doc, "ACKNOWLEDGEMENT", 1, before=24, after=12)
    p = add_paragraph_with_spacing(doc, before=12, after=12)
    p.runs[0].text = (
        "First and foremost, we express our profound gratitude to the Defence Research and Development Organisation (DRDO) "
        "and especially Defence Laboratory Jodhpur (DLJ) for providing this invaluable learning opportunity and a highly "
        "professional research environment. It was an honor to develop a real-world GIS-based defence logistics system "
        "under their supervision."
    )
    p = add_paragraph_with_spacing(doc, before=12, after=12)
    p.add_run(
        "We convey our deepest appreciation and respect to our Internship Mentor, Shri Shyam Lal, Scientist 'F', DLJ. "
        "His expert technical guidance, encouraging insights, and rigorous architecture review sessions were instrumental "
        "throughout the project lifecycle. His mentoring taught us the value of system resilience, geographic coordinate "
        "conventions, database indexing, and enterprise-grade Docker deployments."
    )
    p = add_paragraph_with_spacing(doc, before=12, after=12)
    p.add_run(
        "We also acknowledge the academic departments of our respective universities for supporting our internship "
        "participation and facilitating our technical preparation. Finally, we thank our peers and fellow interns "
        "at DLJ for their collaboration, peer reviews, and creative inputs during the design of our system."
    )
    p = add_paragraph_with_spacing(doc, before=24, after=0)
    p.runs[0].text = "Date: July 2026\nPlace: Jodhpur"
    
    doc.add_page_break()

    # ABSTRACT
    add_heading_with_spacing(doc, "ABSTRACT", 1, before=24, after=12)
    p = add_paragraph_with_spacing(doc, before=12, after=12)
    p.runs[0].text = (
        "The \"Defence Asset Tracking & Geofencing System\" is a production-grade, real-time spatial asset monitoring, "
        "alert generation, and perimeter geofencing management application designed for defence sector logistics and "
        "perimeter security. The main challenge addressed by the system is the dynamic computing of high-frequency GPS "
        "coordinate streams against restricted zone shapes, ensuring low-latency alerts on unauthorized entries or exits. "
        "The system decouples presentation from geometric calculations by employing a standard three-tier architecture: "
        "a lightweight dark-themed SPA client powered by Leaflet.js; a robust Java 17 Servlet backend executing on Apache "
        "Tomcat 9; and a PostgreSQL database extended with PostGIS. Real-time GIS rendering is powered by GeoServer via "
        "WMS (Web Map Service) and WFS (Web Feature Service) protocols, with custom styled layer descriptors (SLD) "
        "managing the visual representation of assets, routes, and polygons."
    )
    p = add_paragraph_with_spacing(doc, before=12, after=12)
    p.add_run(
        "To achieve seamless scalability, standard deployment procedures have been completely dockerized into an "
        "enterprise-grade 3-tier container stack (Tomcat, PostgreSQL+PostGIS, GeoServer) orchestrated via Docker Compose. "
        "A customized configuration sidecar container automates all GeoServer workspaces, PostGIS stores, layer publications, "
        "and style mappings, achieving one-command deployment without requiring pre-installed runtimes. Verification testing "
        "proves the application handles concurrent telemetry ingestion, logs historical paths, compiles KPI metrics, and "
        "flags geofence breaches in sub-second timelines. This report covers the requirement analysis, system architecture, "
        "stored procedural logic, container configuration, and test logs of the completed system."
    )
    
    doc.add_page_break()

    # TABLE OF CONTENTS
    # Table of Contents placeholder (re-generated during final Word field recalculation)
    add_heading_with_spacing(doc, "TABLE OF CONTENTS", 1, before=24, after=12)
    p = add_paragraph_with_spacing(doc, "Table of Contents placeholder - automatic generation enabled", before=12, after=6)
    doc.add_page_break()

    # CHAPTER 1: INTRODUCTION
    add_heading_with_spacing(doc, "CHAPTER 1: INTRODUCTION", 1, before=24, after=12)
    add_heading_with_spacing(doc, "1.1 DRDO Profile Overview", 2, before=12, after=6)
    p = add_paragraph_with_spacing(doc)
    p.runs[0].text = (
        "The Defence Research and Development Organisation (DRDO) represents India's leading military research agency. "
        "Operating under the Department of Defence R&D within the Ministry of Defence, DRDO's vision is to achieve "
        "indigenous defence technology leadership. Over the past six decades, DRDO has expanded to support more than "
        "50 specialized research laboratories across the country, building everything from missile complexes (like APJ Abdul "
        "Kalam Missile Complex) to tactical telemetry systems and spatial monitoring architectures."
    )
    
    add_heading_with_spacing(doc, "1.2 Defence Laboratory Jodhpur Profile", 2, before=12, after=6)
    p = add_paragraph_with_spacing(doc)
    p.runs[0].text = (
        "Defence Laboratory Jodhpur (DLJ) is a major research center under DRDO specializing in materials research, "
        " desert warfare technologies, camouflage systems, and nuclear radiation detection instruments. DLJ is organized into "
        "specialized technical branches:\n"
        "1. **Camouflage Division**: Designs and tests advanced multispectral materials to obscure combat units and command bunkers.\n"
        "2. **Nuclear Radiation Management and Application (NRMA)**: Calibrates and maintains radiation measurement sensors.\n"
        "3. **Desert Environment Science and Technology (DEST)**: Models spatial desert boundaries and logistics paths."
    )

    add_heading_with_spacing(doc, "1.3 Internship Objectives", 2, before=12, after=6)
    p = add_paragraph_with_spacing(doc)
    p.runs[0].text = (
        "The core goals of this collaborative student internship include designing a GIS framework that ingests GPS "
        "telemetry rows, maps boundaries inside database layers, alerts operations when boundaries are violated, "
        "and standardizes deployment operations using multi-container Docker bridge environments."
    )
    
    add_heading_with_spacing(doc, "1.4 Technical Problem Statement", 2, before=12, after=6)
    p = add_paragraph_with_spacing(doc)
    p.runs[0].text = (
        "Tactical command groups require precise real-time positional updates of logistics vehicles and troop units. "
        "Manual logging results in high latency, preventing active response to boundary violations or perimeter alarms. "
        "The objective of this project is to implement an automated system that parses incoming GPS coordinates, checks them "
        "against boundaries inside the database, and renders updates immediately on a mapping client."
    )
    
    doc.add_page_break()

    # CHAPTER 2: LITERATURE SURVEY
    add_heading_with_spacing(doc, "CHAPTER 2: LITERATURE SURVEY", 1, before=24, after=12)
    p = add_paragraph_with_spacing(doc)
    p.runs[0].text = (
        "A detailed survey was conducted comparing key spatial mapping libraries, database engines, and map servers. "
        "Table 2.1 presents a comparative matrix of active technologies evaluated during the research phase."
    )
    
    # Comparison table
    comp_table = doc.add_table(rows=5, cols=4)
    comp_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    comp_table.style = 'Light Shading Accent 1'
    hdr = comp_table.rows[0].cells
    hdr[0].text = "Technology Area"
    hdr[1].text = "Selected Tool"
    hdr[2].text = "Alternative Tool"
    hdr[3].text = "Reason for Selection"
    for cell in hdr:
        set_cell_background(cell, "1A365D")
        cell.paragraphs[0].runs[0].font.color.rgb = RGBColor(255, 255, 255)
        cell.paragraphs[0].runs[0].bold = True
        cell.paragraphs[0].runs[0].font.name = 'Times New Roman'
        set_cell_margins(cell, top=100, bottom=100, left=100, right=100)
        
    tech_rows = [
        ("Web Mapping Engine", "Leaflet.js", "OpenLayers / Google Maps", "Lightweight, easy CSS custom styling, fast performance on mobile views."),
        ("Spatial Database", "PostgreSQL + PostGIS", "MySQL Spatial / Oracle Spatial", "Standard R-tree spatial indexing (GIST) and trigger support."),
        ("GIS Rendering Engine", "GeoServer 2.25", "MapServer / ArcGIS Server", "Open-source, matches WMS/WFS standards, has REST API for configurations."),
        ("Java Web App Container", "Apache Tomcat 9", "GlassFish / JBoss WildFly", "Small RAM usage (under 200MB), fast deploy of WAR package.")
    ]
    for idx, row in enumerate(tech_rows):
        cells = comp_table.rows[idx+1].cells
        for col_idx, text in enumerate(row):
            cells[col_idx].text = text
            cells[col_idx].paragraphs[0].runs[0].font.name = 'Times New Roman'
            cells[col_idx].paragraphs[0].runs[0].font.size = Pt(10)
            set_cell_margins(cells[col_idx], top=80, bottom=80, left=100, right=100)

    doc.add_page_break()

    # CHAPTER 3: REQUIREMENT ANALYSIS
    add_heading_with_spacing(doc, "CHAPTER 3: REQUIREMENT ANALYSIS", 1, before=24, after=12)
    add_heading_with_spacing(doc, "3.1 Functional Requirements specifications", 2, before=12, after=6)
    p = add_paragraph_with_spacing(doc)
    p.runs[0].text = (
        "Functional modules must execute clean interfaces:\n"
        "• **Login Authentication**: Security checks using session token validation.\n"
        "• **Coordinate Ingestion API**: Rest JSON parsing via PositionServlet.\n"
        "• **Boundary Breach Detections**: Spatial trigger computation inside database.\n"
        "• **Alarms Logs & Playback**: Historic travel paths aggregated into LineString."
    )
    
    add_heading_with_spacing(doc, "3.2 User Role Authorization Matrix", 2, before=12, after=6)
    role_table = doc.add_table(rows=4, cols=4)
    role_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    role_table.style = 'Light Shading Accent 1'
    hdr = role_table.rows[0].cells
    hdr[0].text = "Role"
    hdr[1].text = "View Map Dashboard"
    hdr[2].text = "Edit Geofence Zones"
    hdr[3].text = "Access Audit Logs"
    for cell in hdr:
        set_cell_background(cell, "1A365D")
        cell.paragraphs[0].runs[0].font.color.rgb = RGBColor(255, 255, 255)
        cell.paragraphs[0].runs[0].bold = True
        set_cell_margins(cell, top=100, bottom=100, left=100, right=100)
        
    roles_data = [
        ("COMMAND ADMIN", "YES", "YES", "YES"),
        ("OPERATOR USER", "YES", "NO", "NO"),
        ("VIEWER CLIENT", "YES", "NO", "NO")
    ]
    for idx, row in enumerate(roles_data):
        cells = role_table.rows[idx+1].cells
        for col_idx, text in enumerate(row):
            cells[col_idx].text = text
            set_cell_margins(cells[col_idx], top=80, bottom=80, left=100, right=100)
            
    doc.add_page_break()

    # CHAPTER 4: SYSTEM DESIGN & ARCHITECTURE
    add_heading_with_spacing(doc, "CHAPTER 4: SYSTEM DESIGN & ARCHITECTURE", 1, before=24, after=12)
    
    # Render all 18 diagrams with descriptive paragraphs
    diagrams_meta = [
        ("diag_overall_arch.png", "Figure 4.1", "Overall 3-Tier System Architecture Diagram",
         "Figure 4.1 illustrates the decoupled presentation canvas (Leaflet client), control logic (Tomcat), and spatial databases."),
        ("diag_use_case.png", "Figure 4.2", "UML Use Case Interfacing Diagram",
         "Figure 4.2 models permissions boundaries, detailing asset coordinates ingestions, operations panel, and layers admin."),
        ("diag_workflow.png", "Figure 4.3", "Telemetry Ingestion, Spatial Analysis, and Alerts Workflow Diagram",
         "Figure 4.3 details database triggers executing ST_Contains calculation checks to detect restricted zones entry events."),
        ("diag_sequence.png", "Figure 4.4", "Ingestion & Breach Detections Call Sequence Diagram",
         "Figure 4.4 illustrates timelines call loops from assets transceivers, Java servlet APIs, SQL triggers, to map dashboards."),
        ("diag_deployment.png", "Figure 4.5", "Enterprise Deployment Topology with Port Mappings",
         "Figure 4.5 maps TCP server endpoints (ports 8080, 8085, 5432) linking compose layers container runtimes."),
        ("diag_activity.png", "Figure 4.6", "Operator Dashboard Activities Workflow Flowchart",
         "Figure 4.6 maps dashboard sessions tracking steps including auth, overlays load, alarms check, and pdf export."),
        ("diag_geoserver_flow.png", "Figure 4.7", "GeoServer tile Rendering Request Pipeline",
         "Figure 4.7 outlines map requests WMS parsing WMS format layouts and drawing geometry shapes dynamically via SLD definitions."),
        ("diag_rest_flow.png", "Figure 4.8", "REST Ingestion Database Pipeline",
         "Figure 4.8 highlights position parser HTTP servlet interactions writing coordinate nodes through HikariCP databases."),
        ("diag_component.png", "Figure 4.9", "UML Component Diagram of WebGIS Interfaces",
         "Figure 4.9 details software components boundaries spanning map JS client, REST controller JVM, and PostGIS server instances."),
        ("diag_class.png", "Figure 4.10", "UML Class Entity Structures and Associations Diagram",
         "Figure 4.10 models coordinate entities, asset registrations, alert models, user session instances, and database pools classes."),
        ("diag_state.png", "Figure 4.11", "Tactical Asset Operational States Diagram",
         "Figure 4.11 displays coordinate transitions tracking registered combat units from online tracking to SOS breach alarms states."),
        ("diag_auth_flow.png", "Figure 4.12", "Authentication Session Validation Flowchart",
         "Figure 4.12 maps login routes matching input details against stored hashed passwords via BCrypt blowfish algorithm."),
        ("diag_db_flow.png", "Figure 4.13", "Database Row Insert and Trigger Flow",
         "Figure 4.13 details PostgreSQL table checks inserting positions, evaluating boundaries overlap, and writing alarms."),
        ("diag_container.png", "Figure 4.14", "Docker Compose Multi-Container Boundary Layout",
         "Figure 4.14 illustrates application system limits isolating web, database, rendering engines, and config sidecar services."),
        ("diag_module_dependency.png", "Figure 4.15", "Software Modules Dependency Directions Layout",
         "Figure 4.15 shows dependency directions mapping presentation UI hooks to control controllers and persistent entities."),
        ("diag_network.png", "Figure 4.16", "Docker Bridge Subnet and Ports Mappings Configuration",
         "Figure 4.16 maps virtual subnet nodes communicating over isolated defence bridge interfaces."),
        ("diag_communication.png", "Figure 4.17", "UML Communication Diagram of Ingestion Calls Sequence",
         "Figure 4.17 shows numbered communication links passing coordinates payloads from field trackers to database tables.")
    ]
    
    for filename, label, caption, desc in diagrams_meta:
        img_path = os.path.join(workspace_root, "docs", "images", filename)
        if os.path.exists(img_path):
            doc.add_paragraph().alignment = WD_ALIGN_PARAGRAPH.CENTER
            doc.paragraphs[-1].add_run().add_picture(img_path, width=Inches(5.5))
            add_caption(doc, label, caption)
            p = add_paragraph_with_spacing(doc)
            p.runs[0].text = desc
            p.runs[0].italic = True
            doc.add_page_break()

    # Insert Database ERD Full Page
    erd_path = os.path.join(workspace_root, "docs", "database_erd.png")
    if os.path.exists(erd_path):
        doc.add_paragraph().alignment = WD_ALIGN_PARAGRAPH.CENTER
        doc.paragraphs[-1].add_run().add_picture(erd_path, width=Inches(6.0))
        add_caption(doc, "Figure 4.18", "Entity Relationship Diagram (ERD) of Defence GIS database")
        p = add_paragraph_with_spacing(doc)
        p.runs[0].text = "Figure 4.18 maps DDL schema tables primary/foreign key connections and GIS geographic coordinates field types."
        p.runs[0].italic = True
        doc.add_page_break()

    # CHAPTER 5: DETAILED TECHNOLOGY STACK
    add_heading_with_spacing(doc, "CHAPTER 5: DETAILED TECHNOLOGY STACK", 1, before=24, after=12)
    # Technical descriptions
    p = add_paragraph_with_spacing(doc)
    p.runs[0].text = (
        "Each technology layer was evaluated based on stability, memory constraints, and compliance with military GIS formats:\n"
        "• **Java 17 JRE**: Strongly-typed platform hosting Web Servlets with minimal resource usage.\n"
        "• **PostgreSQL 16 + PostGIS 3.4**: Robust spatial queries using ST_Contains and R-tree indexes.\n"
        "• **GeoServer 2.25**: Translates geographic tables to standard WMS/WFS vector images.\n"
        "• **Leaflet.js client**: Fast single page layouts executing coordinates overlays updates under sub-second timelines."
    )
    doc.add_page_break()

    # CHAPTER 6: PROJECT IMPLEMENTATION
    add_heading_with_spacing(doc, "CHAPTER 6: PROJECT IMPLEMENTATION", 1, before=24, after=12)
    add_heading_with_spacing(doc, "6.1 Authentication & Filter Gateways", 2, before=12, after=6)
    p = add_paragraph_with_spacing(doc)
    p.runs[0].text = (
        "Session checking is handled by `AuthFilter.java` guarding direct REST endpoints access paths."
    )
    add_code_block(doc, 
        "// AuthFilter.java - Session check snippet\n"
        "public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {\n"
        "    HttpServletRequest req = (HttpServletRequest) request;\n"
        "    HttpServletResponse resp = (HttpServletResponse) response;\n"
        "    String path = req.getRequestURI();\n"
        "    if (path.contains(\"/api/auth/login\")) {\n"
        "        chain.doFilter(request, response);\n"
        "        return;\n"
        "    }\n"
        "    HttpSession session = req.getSession(false);\n"
        "    if (session == null || session.getAttribute(\"userId\") == null) {\n"
        "        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);\n"
        "        return;\n"
        "    }\n"
        "    chain.doFilter(request, response);\n"
        "}"
    )
    
    add_heading_with_spacing(doc, "6.2 Real-Time Asset Ingestion Loop", 2, before=12, after=6)
    p = add_paragraph_with_spacing(doc)
    p.runs[0].text = (
        "AJAX polling routines query API positions paths and draw updates."
    )
    add_code_block(doc,
        "// loadAssetMarkers in map.js\n"
        "loadAssetMarkers: function() {\n"
        "    fetch('/DefenceGIS/api/positions/latest')\n"
        "        .then(res => res.json())\n"
        "        .then(res => {\n"
        "            if (res.status === 'success') {\n"
        "                self.drawMarkers(res.data);\n"
        "            }\n"
        "        });\n"
        "}"
    )
    doc.add_page_break()

    # CHAPTER 7: DATABASE DESIGN
    add_heading_with_spacing(doc, "CHAPTER 7: DATABASE DESIGN", 1, before=24, after=12)
    add_heading_with_spacing(doc, "7.1 DDL Schema scripts", 2, before=12, after=6)
    add_code_block(doc,
        "-- Database tables structures\n"
        "CREATE TABLE assets (\n"
        "    id SERIAL PRIMARY KEY,\n"
        "    asset_name VARCHAR(100) NOT NULL,\n"
        "    asset_code VARCHAR(30) UNIQUE NOT NULL\n"
        ");\n\n"
        "CREATE TABLE asset_positions (\n"
        "    id SERIAL PRIMARY KEY,\n"
        "    asset_id INTEGER REFERENCES assets(id),\n"
        "    geom GEOMETRY(Point, 4326) NOT NULL,\n"
        "    recorded_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()\n"
        ");"
    )
    doc.add_page_break()

    # CHAPTER 8: DOCKER CONTAINERIZATION
    add_heading_with_spacing(doc, "CHAPTER 8: DOCKER CONTAINERIZATION", 1, before=24, after=12)
    p = add_paragraph_with_spacing(doc)
    p.runs[0].text = (
        "Docker compose orchestrates Tomcat application servers, Postgres databases, GeoServer, and setups scripts."
    )
    add_code_block(doc,
        "# docker-compose.yml configuration outline\n"
        "version: '3.8'\n"
        "services:\n"
        "  postgres:\n"
        "    image: postgis/postgis:16-3.4\n"
        "    volumes:\n"
        "      - defence-pgdata:/var/lib/postgresql/data\n"
        "  tomcat:\n"
        "    build: .\n"
        "    ports:\n"
        "      - \"8080:8080\""
    )
    doc.add_page_break()

    # CHAPTER 9: VERIFICATION & TESTING
    add_heading_with_spacing(doc, "CHAPTER 9: VERIFICATION & TESTING", 1, before=24, after=12)
    p = add_paragraph_with_spacing(doc)
    p.runs[0].text = (
        "Structured validation runs verified system operations:"
    )
    # Testing matrix table
    test_table = doc.add_table(rows=4, cols=4)
    test_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    test_table.style = 'Light Shading Accent 1'
    hdr = test_table.rows[0].cells
    hdr[0].text = "Test Case ID"
    hdr[1].text = "Target Module"
    hdr[2].text = "Expected Result"
    hdr[3].text = "Actual Result / Status"
    for cell in hdr:
        set_cell_background(cell, "1A365D")
        cell.paragraphs[0].runs[0].font.color.rgb = RGBColor(255, 255, 255)
        set_cell_margins(cell, top=100, bottom=100, left=100, right=100)
        
    tests = [
        ("TC001", "AuthFilter checking", "Bypassing routes without session returns HTTP 401.", "PASS / 401 Returned"),
        ("TC002", "GPS Ingest checks", "POST coordinates write coordinates into positions table.", "PASS / Row inserted"),
        ("TC003", "Geofence Trigger", "Entering restricted polygons writes alert rows.", "PASS / ST_Contains fired")
    ]
    for idx, row in enumerate(tests):
        cells = test_table.rows[idx+1].cells
        for col_idx, text in enumerate(row):
            cells[col_idx].text = text
            set_cell_margins(cells[col_idx], top=80, bottom=80, left=100, right=100)
            
    doc.add_page_break()

    # CHAPTER 10: RESULTS & OUTPUTS
    add_heading_with_spacing(doc, "CHAPTER 10: RESULTS & OUTPUTS", 1, before=24, after=12)
    
    # HD screenshots insertions
    screenshots = [
        ("geoserver_welcome.png", "Figure 10.1", "GeoServer Web Console welcome dashboard"),
        ("geoserver_layers.png", "Figure 10.2", "GeoServer layers preview listing vector targets"),
        ("landing.png", "Figure 10.3", "Tactical portal welcome landing portal layout"),
        ("login.png", "Figure 10.4", "Secure credentials credentials session validation form"),
        ("dashboard.png", "Figure 10.5", "Real-Time active asset counters dashboard metrics"),
        ("tracking.png", "Figure 10.6", "Interactive Leaflet mapping telemetry tracker overlay"),
        ("geofence.png", "Figure 10.7", "Restricted zone polygons styled borders layout"),
        ("alerts.png", "Figure 10.8", "Breach alarms logs grid audit registry records"),
        ("reports.png", "Figure 10.9", "Reports query compiler and data logs exporter panel")
    ]
    for filename, label, caption in screenshots:
        img_path = os.path.join(workspace_root, "docs", "images", filename)
        if os.path.exists(img_path):
            doc.add_paragraph().alignment = WD_ALIGN_PARAGRAPH.CENTER
            doc.paragraphs[-1].add_run().add_picture(img_path, width=Inches(5.0))
            add_caption(doc, label, caption)
            p = add_paragraph_with_spacing(doc)
            p.runs[0].text = f"HD layout screenshot showcasing {caption} details from the active runtime container services."
            p.runs[0].italic = True
            doc.add_page_break()

    # CHAPTER 11: LEARNING OUTCOMES
    add_heading_with_spacing(doc, "CHAPTER 11: LEARNING OUTCOMES", 1, before=24, after=12)
    p = add_paragraph_with_spacing(doc)
    p.runs[0].text = (
        "Internship learning highlights include mapping vector layers, configuring multi-stage Docker builds, "
        "managing PostGIS spatial databases triggers, and working in collaborations on defensive software architectures."
    )
    doc.add_page_break()

    # CHAPTER 12: CONCLUSION
    add_heading_with_spacing(doc, "CHAPTER 12: CONCLUSION", 1, before=24, after=12)
    p = add_paragraph_with_spacing(doc)
    p.runs[0].text = (
        "The Defence Asset Tracking & Geofencing System provides low-latency spatial intelligence monitoring panel "
        "configured automatically via Docker Compose, matching OGC specifications and DRDO guidelines."
    )
    doc.add_page_break()

    # APPENDIX
    add_heading_with_spacing(doc, "APPENDIX", 1, before=24, after=12)
    add_heading_with_spacing(doc, "Appendix A: Complete REST API specification", 2, before=12, after=6)
    p = add_paragraph_with_spacing(doc)
    p.runs[0].text = (
        "• GET `/api/dashboard`: JSON statistics parameters.\n"
        "• GET `/api/positions/latest`: GeoJSON coordinates nodes list.\n"
        "• GET `/api/alerts`: Active alerts list.\n"
        "• GET `/api/geofences`: Defined restricted polygon coordinates."
    )
    doc.add_page_break()

    # REFERENCES
    add_heading_with_spacing(doc, "REFERENCES", 1, before=24, after=12)
    refs = [
        "[1] Open Geospatial Consortium, \"Web Map Service WMS Standards Manual v1.3.0,\" 2006.",
        "[2] R. Obe and L. Hsu, *PostGIS in Action*, Third Edition, Manning publications, 2021.",
        "[3] Docker Inc., \"Docker Compose File Format specification v3.8,\" 2023.",
        "[4] PostgreSQL Global Development Group, \"PostgreSQL Database Manuals v16,\" 2023."
    ]
    for ref in refs:
        p = add_paragraph_with_spacing(doc, ref, line_spacing=1.15, align=WD_ALIGN_PARAGRAPH.LEFT)

    output_docx = os.path.join(workspace_root, "DRDO_Internship_Report.docx")
    doc.save(output_docx)
    print(f"Report DOCX successfully generated at: {output_docx}")

if __name__ == '__main__':
    main()
