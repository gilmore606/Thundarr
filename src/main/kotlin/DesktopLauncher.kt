import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration

object DesktopLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val config = LwjglApplicationConfiguration().apply {
            title = "Thundarr"
            width = 1200
            height = 900
        }
        LwjglApplication(App, config)
    }
}
