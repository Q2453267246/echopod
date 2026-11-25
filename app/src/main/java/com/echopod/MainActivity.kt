package com.echopod

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.echopod.core.designsystem.theme.EchoPodTheme
import com.echopod.presentation.shell.EchoPodApp

/**
 * 应用唯一的 Activity。
 *
 * - enableEdgeToEdge()：内容绘制到状态栏 / 手势条后面，由外壳与各页面按需处理 WindowInsets，
 *   因此这里**不要**整体加 safeDrawingPadding()，否则背景色无法延伸到系统栏后面；
 * - 折叠/展开、旋转会触发 Activity 重建（Manifest 中没有锁定方向，也没有声明 configChanges），
 *   导航返回栈与页面滚动位置通过 rememberSaveable / NavHost 的 saveState 自动恢复。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EchoPodTheme {
                EchoPodApp()
            }
        }
    }
}
