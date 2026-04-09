import os
import re
import glob

fxml_dir = r"testFile\src\main\resources\com\example\testfile"
java_dir = r"testFile\src\main\java\com\example\testfile"

def update_java_files():
    for java_file in glob.glob(os.path.join(java_dir, "*.java")):
        with open(java_file, "r", encoding="utf-8") as f:
            content = f.read()
        
        # Replace new Scene(..., 700, 500)
        content = re.sub(r"new Scene\(([^,]+),\s*700\s*,\s*500\s*\)", r"new Scene(\1, 1152, 768)", content)
        # Also any width, height from welcome controller
        content = re.sub(r"new Scene\(([^,]+),\s*width\s*,\s*height\s*\)", r"new Scene(\1, 1152, 768)", content)
        # Any openScene(..., 700, 500)
        content = re.sub(r"openScene\(([^,]+),\s*([^,]+),\s*700\s*,\s*500\s*\)", r"openScene(\1, \2, 1152, 768)", content)
        
        # HelloApplication.java explicit scene size
        if "HelloApplication.java" in java_file:
            pass # already handled by regex maybe? Wait, HelloApplication has 1152, 768.

        with open(java_file, "w", encoding="utf-8") as f:
            f.write(content)

def generate_nav(active_page):
    items = [
        ("Home", "welcome"),
        ("Courses", "courses"),
        ("About", "about"),
        ("Login", "login")
    ]
    
    # Check if active is one of these
    active_labels = {'welcome': 'Home', 'user-home': 'Home', 'courses':'Courses', 'about':'About', 'login':'Login', 'role':'Login', 'signup':'Login'}
    active_name = active_labels.get(active_page, "Home")
    
    xml = []
    xml.append('<HBox alignment="CENTER_RIGHT" spacing="30" AnchorPane.topAnchor="30" AnchorPane.rightAnchor="48">')
    for text, page_id in items:
        if text == active_name:
            xml.append(f'    <Label text="{text}" styleClass="top-nav-item top-nav-active"/>')
        else:
            xml.append(f'    <Button text="{text}" styleClass="top-nav-btn"/>')
    
    xml.append('</HBox>')
    return "\n            ".join(xml)

def update_fxml_files():
    for fxml_file in glob.glob(os.path.join(fxml_dir, "*.fxml")):
        # Skip hello-view.fxml as it seems irrelevant or we modify all? The prompt says "Inside every FXML file (about, courses, login, role, signup, user-home)"
        page_id = os.path.basename(fxml_file).replace(".fxml", "")
        if page_id not in ['about', 'courses', 'login', 'role', 'signup', 'user-home', 'welcome']:
            continue
            
        with open(fxml_file, "r", encoding="utf-8") as f:
            content = f.read()

        # Find imports and XML decl
        imports_decl = ""
        decl_match = re.search(r'<\?xml.*?\?>', content)
        if decl_match:
            imports_decl += decl_match.group(0) + "\n\n"
        
        imports = re.findall(r'<\?import.*?\?>', content)
        if imports:
            imports_decl += "\n".join(imports) + "\n\n"
        
        # Extract the root element string
        # We need to remove imports and xml decl to parse easier
        body = re.sub(r'<\?.*?\?>', '', content).strip()
        
        controller_match = re.search(r'fx:controller="([^"]+)"', body)
        controller_str = f' fx:controller="{controller_match.group(1)}"' if controller_match else ""
        
        # Strip fx:controller and xmlns:fx from current root
        body = re.sub(r'\s*xmlns:fx="[^"]+"', '', body, count=1)
        body = re.sub(r'\s*fx:controller="[^"]+"', '', body, count=1)
        
        nav_xml = generate_nav(page_id)
        
        new_content = f"""{imports_decl}<StackPane styleClass="welcome-root" xmlns:fx="http://javafx.com/fxml"{controller_str}>
    <AnchorPane maxWidth="1120" maxHeight="700" prefWidth="1080" prefHeight="650" styleClass="landing-panel">
        {nav_xml}
        {body}
    </AnchorPane>
</StackPane>
"""
        with open(fxml_file, "w", encoding="utf-8") as f:
            f.write(new_content)

if __name__ == "__main__":
    update_java_files()
    update_fxml_files()
    print("Done")
