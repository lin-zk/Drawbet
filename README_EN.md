[中文](README.md) | English | [日本語](README_JA.md)

# Drawbet - Simple Drawing App

---

## 📱 Overview

Drawbet is an intuitive Android drawing application featuring multiple brush styles, color customization, and drawing history management. Perfect for capturing creative inspiration anytime, anywhere.

---

## 📖 Features

### 🏠 Home Page

<img src="picture/首页.png" width="549" alt="Home Page">

- **New Drawing**: Create a new drawing with custom naming (default format: `NewCanvas_CurrentTime`)
- **Continue Drawing**: Quickly load your most recently edited drawing
- **Drawing History**: Browse and manage all your saved drawings
- Features a beautiful illustrated background with a semi-transparent overlay that complements the drawing theme
- Author information displayed at the bottom

### 🎨 Drawing Page

<img src="picture/作画页面.png" width="562" alt="Drawing Page">

- **Drawing Tools**: Four drawing modes to choose from
  - Brush: Draw freehand curves
  - Eraser: Remove drawn content
  - Circle: Draw circles and ellipses
  - Rectangle: Draw rectangular shapes
- **Brush Size Adjustment**: Use the slider to adjust brush thickness in real-time, with a preview circle on the left showing the current size
- **Color Adjustment**: Customize brush colors using RGB sliders, with the left preview circle displaying the current color in real-time
- **Undo/Redo**: Support for multiple undo and redo operations
- **Clear Canvas**: Clear the entire canvas with one click (confirmation required)
- **Auto-save**: Automatically saves changes to the database when modifications are detected
- **Export Image**: Tap the save button to export your drawing as a JPG image to your phone's gallery; long press to navigate to the history page
- **Shake to Clear**: Shake your device to quickly clear the canvas (confirmation required)
- **Rename**: Tap the drawing title at the top to rename it
- **State Preservation**: Automatically saves the undo/redo stack when changes are made

### 📂 History Page

<img src="picture/历史画作页面.png" width="562" alt="History Page">

- **Search Functionality**: Search as you type with matching portions of drawing names highlighted in red
- **Create New Drawing**: Tap the `+` button in the top-right corner to create a new drawing
- **Drawing List**:
  - Displays thumbnails, names, and last modified timestamps
  - Tap a name to rename the drawing
  - Tap a drawing to open it for editing
  - Checkboxes enable batch selection
- **Batch Operations**:
  - Select All/Deselect All: Toggle selection of all drawings with one tap
  - Invert Selection: Reverse the current selection
  - Delete: Remove selected drawings in bulk
  - Save: Export selected drawings as JPG images to your phone's gallery
- When no drawings exist, displays "No drawings yet, tap + in the top-right to create one"
- Supports scrolling to browse through large collections of drawings

### 👤 About Page

<img src="picture/关于页面.png" width="562" alt="About Page">

- Long press the "Clear" button to access the About page
- Displays app version information and developer details
- Clean interface design showcasing essential app information

---

## 🛠️ Technical Implementation

### Project Structure
```
app/src/main/java/com/example/majordesign_master_v1/
├── MainActivity.java        # Main drawing page logic
├── HomeActivity.java        # Home page logic
├── HistoryActivity.java     # History page logic
├── WrittingView.java        # Custom drawing view
├── AboutPage.java           # About page
├── data/                    # Data layer
│   ├── DrawingDao.java      # Data Access Object
│   ├── DrawingDatabase.java # Room database
│   ├── DrawingEntity.java   # Drawing entity
│   ├── DrawingRepository.java # Data repository
│   ├── DrawingState.java    # Drawing state
│   └── BitmapConverters.java # Bitmap conversion utilities
└── history/                 # History UI
    ├── HistoryAdapter.java  # List adapter
    └── HistoryViewModel.java # ViewModel
```

### Key Technologies
- **Layout**: Linear vertical layout using LinearLayout
- **Custom View**: WrittingView implements canvas functionality
- **Data Storage**: Room database for persistent drawing data storage
- **Style Resources**: Drawable resources for rounded rectangles, circular views, gradient backgrounds, etc.
- **Sensors**: Accelerometer sensor for shake-to-clear functionality

### 🗂️ Data & Storage
- **Auto-save**: The undo/redo stack is automatically persisted every 30 seconds while drawing, and whenever the app moves to the background or exits
- **Local Database**: Room stores drawing names, thumbnails, and canvas state (`DrawingState` / `BitmapConverters`)
- **Image Export**: Saves JPG files to the system gallery via MediaStore using the drawing name; defaults to `NewCanvas_yyyyMMdd_HHmmss` for unnamed drawings
- **Permissions**: Exporting images requires gallery write access; on Android 10+, this uses scoped storage without requiring legacy permissions

### Development Overview

1. Build three main pages: Home, Drawing, and History
2. Design the home page layout with three entry points: New Drawing, Continue Drawing, and Drawing History
3. Design the drawing page including brush size adjustment, style selection, canvas, color adjustment, and action buttons
4. Design the history page with search, create, and batch operation capabilities
5. Create drawable and style resources for polished UI effects
6. Implement MainActivity interaction logic
7. Implement WrittingView drawing functionality
8. Implement HomeActivity behavior
9. Implement HistoryActivity for managing drawing history
10. Create data and history packages for database and UI interface management
11. Design the About page

---

## 🚀 Running the Project

### Requirements
- Android Studio 2024.1+ (Android Gradle Plugin 8 compatible)
- Android SDK: compileSdk 36 / targetSdk 36, minSdk 24
- JDK 11
- Gradle Wrapper 8.13 (included)

### Build Steps
```bash
# Clone the project
git clone https://github.com/lin-zk/Drawbet.git
cd Drawbet
# Build the project
./gradlew build
```

---

## 📄 License

This project is released under the **[MIT License](LICENSE)**.

---

## 🤝 About the Author
- **Author**: lin-zk
- **Email**: 1751740699@qq.com / eezhengkanglin@mail.scut.edu.cn
- **QQ**: 1751740699
- **Feel free to reach out**

---

## 🙏 Acknowledgments

- **Copilot's powerful code generation and debugging capabilities have been instrumental in bringing this project to fruition**
- **[「ご注文はうさぎですか？」New Anime Production Announcement Celebration Artwork](https://gochiusa.com/anime10th/contents/c06080000.html?utm_source=x&utm_medium=post&utm_campaign=tp2025) used as the home page illustration background**
- **The Embedded Systems and Mobile Application Design course offered by SCUT and its two dedicated instructors**
