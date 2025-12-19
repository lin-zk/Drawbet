# Drawbet - Simple Drawing App

**Language / 语言选择 / 言語:**  [中文](README.md) | [English](#) | [日本語](README_JA.md)

---

## 📱 About

Drawbet is a simple and easy-to-use Android drawing application that supports multiple brush styles, color adjustments, and drawing history management. Perfect for capturing creative inspirations anytime, anywhere.

---

## 📖 Features

### 🏠 Home Page
- **New Drawing**: Create a new drawing with custom naming (default format: `NewDrawing_CurrentTime`)
- **Continue Drawing**: Quickly load the most recently edited drawing
- **History**: Browse and manage all saved drawings
- Beautiful illustration background with semi-transparent overlay, aesthetically pleasing and theme-appropriate
- Author information displayed at the bottom

### 🎨 Drawing Page
- **Drawing Tools**: Four drawing modes available
  - Brush: Free-form line drawing
  - Eraser: Erase drawn content
  - Circle: Draw ellipses/circles
  - Rectangle: Draw rectangles
- **Brush Size**: Adjust brush thickness via slider with real-time preview on the left circular view
- **Color Adjustment**: Freely adjust brush color using RGB sliders with real-time preview on the left circular view
- **Undo/Redo**: Support for multi-step undo and redo operations
- **Clear Canvas**: One-click canvas clearing (confirmation required)
- **Save Drawing**: Save drawing to database
- **Export Image**: Long press the save button to go to history page; short press to export drawing as JPG to phone gallery
- **Shake to Clear**: Shake the phone to quickly clear the canvas (confirmation required)
- **Rename**: Click on the drawing title at the top to rename
- **Auto-save**: Automatically saves undo/redo stacks when drawing changes
- Author information displayed at the bottom

### 📂 History Page
- **Search**: Instant search as you type, matching parts of drawing names are highlighted in red
- **New Drawing**: Click the `+` button in the top right to create a new drawing
- **Drawing List**:
  - Displays thumbnail, name, and last modified time
  - Click on name to rename
  - Click on drawing to enter edit mode
  - Checkbox support for batch selection
- **Batch Operations**:
  - Select All/Deselect: One-click select all or deselect
  - Invert: Invert current selection
  - Delete: Batch delete selected drawings
  - Export: Batch export selected drawings as JPG images to phone gallery
- Shows "No drawings, click + in top right to create" when empty
- Scroll support for browsing many drawings
- Author information displayed at the bottom

### 👤 About Page
- Long press the "Clear" button to access the About page
- Displays author information

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

### Development Steps Summary

1. Build three main pages: Home, Drawing, and History pages
2. Design home page layout with three entry points: New Drawing, Continue Drawing, History
3. Design drawing page with brush size adjustment, brush style selection, canvas, color adjustment, and action buttons
4. Design history page with search, create new, and batch operation features
5. Create drawable and style resources for beautiful UI effects
6. Implement MainActivity interaction logic
7. Implement WrittingView drawing functionality
8. Implement HomeActivity home page behavior
9. Implement HistoryActivity for history management
10. Create data and history packages for database and UI interface management
11. Design About page

---

## 🚀 Running the Project

### Requirements
- Android Studio Arctic Fox or higher
- Android SDK 21 (Android 5.0) or higher
- Gradle 7.0+

### Build Steps
```bash
# Clone the project
git clone https://github.com/lin-zk/Drawbet.git
cd Drawbet

# Build the project
./gradlew build

# Run tests
./gradlew test
```

---

## 📄 License

This project is for learning and educational purposes only.

---

**Author**: lin-zk
