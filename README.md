中文 | [English](README_EN.md) | [日本語](README_JA.md)

# Drawbet - 简易绘画应用
---

## 📱 应用简介

Drawbet 是一款简洁易用的 Android 绘画应用，支持多种画笔样式、颜色调节、历史画作管理等功能。适合随时随地记录创意灵感。

---

## 📖 功能特点

### 🏠 首页
- **新的画作**：创建新画作，可自定义命名（默认命名格式：`新建画布_当前时间`）
- **继续作画**：快速载入最近一次编辑的画作
- **历史画作**：浏览和管理所有保存的画作
- 精美的插图背景搭配半透明叠加层，美观且符合绘画主题
- 底部显示作者个人信息

### 🎨 作画页面
- **画笔工具**：提供四种绘画模式
  - 画笔：自由绘制曲线
  - 橡皮：擦除已绘制内容
  - 圆形：绘制椭圆/圆形
  - 矩形：绘制矩形
- **笔大小调节**：通过滑动条实时调整笔刷粗细，左侧圆形预览区实时显示当前大小
- **颜色调节**：通过 RGB 三色滑动条自由调整画笔颜色，左侧圆形预览区实时显示当前颜色
- **撤销/恢复**：支持多步撤销和恢复操作
- **清空画布**：一键清空当前画作（需确认）
- **自动保存画作**：检测画作变化自动保存到数据库
- **导出图片**：点击保存按钮可将画作导出为 JPG 图片到手机相册，长按保存按钮可跳转至历史画作页面
- **摇晃清屏**：摇晃手机可快速清空画布（需确认）
- **重命名**：点击顶部画作名称可进行重命名
- **自动保存**：画作变化时自动保存撤销/恢复栈

### 📂 历史画作页面
- **搜索功能**：输入文本即时搜索，匹配的画作名称部分会高亮标红
- **新建画作**：点击右上角 `+` 按钮新建画作
- **画作列表**：
  - 显示画作缩略图、名称和最近修改时间
  - 点击画作名称可重命名
  - 点击画作可进入编辑
  - 支持复选框批量选择
- **批量操作**：
  - 全选/取消：一键全选或取消选择
  - 反选：反转当前选择状态
  - 删除：批量删除选中的画作
  - 保存：批量导出选中画作为 JPG 图片到手机相册
- 无画作时显示"暂无画作，点击右上角 + 新建"
- 支持滑动浏览大量画作

### 👤 关于页面
- 长按"清空"按钮可进入关于页面

---

## 🛠️ 技术实现

### 项目结构
```
app/src/main/java/com/example/majordesign_master_v1/
├── MainActivity.java        # 作画页面主逻辑
├── HomeActivity.java        # 首页逻辑
├── HistoryActivity.java     # 历史画作页面逻辑
├── WrittingView.java        # 自定义绘画视图
├── AboutPage.java           # 关于页面
├── data/                    # 数据层
│   ├── DrawingDao.java      # 数据访问对象
│   ├── DrawingDatabase.java # Room 数据库
│   ├── DrawingEntity.java   # 画作实体
│   ├── DrawingRepository.java # 数据仓库
│   ├── DrawingState.java    # 画作状态
│   └── BitmapConverters.java # 位图转换工具
└── history/                 # 历史画作 UI
    ├── HistoryAdapter.java  # 列表适配器
    └── HistoryViewModel.java # ViewModel
```

### 主要技术
- **布局**：使用 LinearLayout 线性垂直布局
- **自定义视图**：WrittingView 实现画板功能
- **数据存储**：Room 数据库持久化画作数据
- **样式资源**：drawable 资源实现圆角矩形、圆形视图、渐变背景等效果
- **传感器**：加速度传感器实现摇晃清屏功能

### 🗂️ 数据与存储
- **自动保存**：进入作画页后每 30 秒自动落盘撤销/恢复栈，切后台或退出时也会保存
- **本地数据库**：Room 存储画作名称、缩略图与状态（`DrawingState`/`BitmapConverters`）
- **图片导出**：通过 MediaStore 以 JPG 保存到系统相册，文件名默认采用画作名，未命名时使用 `新建画布_yyyyMMdd_HHmmss`
- **权限提示**：导出图片需要允许相册写入权限（Android 10+ 使用分区存储，无需 legacy 权限）

### 开发步骤摘要

1. 构建三个主要页面：首页、作画页面、历史画作页面
2. 设计首页布局，实现新建画作、继续作画、历史画作三个功能入口
3. 设计作画页面，包含笔大小调节、笔样式选择、画板、颜色调节和操作按钮
4. 设计历史画作页面，支持搜索、新建、批量操作等功能
5. 编写 drawable 和 style 资源实现美观的 UI 效果
6. 实现 MainActivity 的交互逻辑
7. 实现 WrittingView 的绘画功能
8. 实现 HomeActivity 的首页行为
9. 实现 HistoryActivity 的历史画作管理
10. 建立 data 和 history 包管理数据库和 UI 相关接口
11. 设计关于页面

---

## 🚀 运行项目

### 环境要求
- Android Studio 2024.1+（支持 Android Gradle Plugin 8）
- Android SDK：compileSdk 36 / targetSdk 36，minSdk 24
- JDK 11
- Gradle Wrapper 8.13（项目已内置）

### 构建步骤
```bash
# 克隆项目
git clone https://github.com/lin-zk/Drawbet.git
cd Drawbet
# 构建项目
./gradlew build
```

---

## 📄 许可证

本项目使用 **[MIT许可证](LICENSE)**。

---

## 🤝 关于作者
- **作者**：lin-zk
- **电子邮箱**：1751740699@qq.com / eezhengkanglin@mail.scut.edu.cn
- **QQ**:1751740699
- **欢迎交流**

---

## 🙏 特别感谢

- **Copilot强大的代码构建与debug能力，为项目顺利实现贡献巨大**
- **[「ご注文はうさぎですか？」新作アニメ制作決定！贺图](https://gochiusa.com/anime10th/contents/c06080000.html?utm_source=x&utm_medium=post&utm_campaign=tp2025)做为本项目首页插图背景**
- **SCUT开设的嵌入式系统与移动应用设计这门课程以及两位授课老师**
