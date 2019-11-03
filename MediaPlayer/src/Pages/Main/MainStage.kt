package Pages.Main

import Pages.Settings.SettingsController
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Stage

class MainStage : Stage() {
    private var loader : FXMLLoader = FXMLLoader()

    init {
        loader.location = javaClass.getResource("Main.fxml")
        var mainScene = loader.load<Parent>()
        val controller = loader.getController<MainController>()
        controller.mainBlock = (mainScene.lookup("#mainVBox") as VBox)
        controller.musicSlider = (mainScene.lookup("#musicSlider") as Slider)
        controller.volumeSlider = (mainScene.lookup("#volumeSlider") as Slider)
        controller.musicTimer = (mainScene.lookup("#musicTimer") as Text)
        controller.musicName = (mainScene.lookup("#musicName") as Text)
        controller.init()

        scene = Scene(mainScene)
    }
}