package Pages.Main

import FolderReader
import Model
import Music
import Pages.Settings.SettingsStage
import javafx.beans.Observable
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.scene.chart.XYChart
import javafx.scene.media.AudioSpectrumListener
import javafx.util.Duration
import java.nio.file.Paths
import javafx.scene.control.SplitPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import src.UI.EqualizerHBox
import src.UI.MusicInfoTextBox
import src.UI.MusicFromDirectoryTable

class MainController : Model() {

    fun init(){
        musicTableTurnOn()
        EqualizerHBox.getBox() // Equalizer init.

        for(i in 0 until numOfBars){ // Spectrum preparation. (Вынести отдельно)
            spectrumData.data.add(XYChart.Data<String, Number>(i.toString(), 0))
        }
        spectrumBarChart.data.add(spectrumData)

        Thread(Runnable {
            while (true) {
                if (player.isInitialized()) {
                    val currentTime = player.getCurrentTime().toSeconds()
                    val allTime = player.getEndTime().toSeconds()

                    musicTimer.text = "${(currentTime / 60).toInt()}.${(currentTime % 60).toInt()} / ${player.playingMusic.getDuration()}"
                    musicName.text = "${player.playingMusic.getName()}"
                    musicSlider.value = currentTime * 100.0 / allTime
                }else{
                    musicName.text = "Music name..."
                    musicTimer.text = "Timer..."
                    musicSlider.value = 0.0
                }

                try {
                    Thread.sleep(1000)
                    tableViewMusic.refresh()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }).start()

        player.mediaPlayerSetUp = {mediaPlayer ->
            volumeSliderDragged()

            mediaPlayer.audioSpectrumInterval = 0.02
            mediaPlayer.audioSpectrumNumBands = numOfBars
            mediaPlayer.audioSpectrumListener = AudioSpectrumListener { d, d2, magnitudes, phases -> // Spectrum listener.
                for(i in 0 until numOfBars){
                    var newValue = (magnitudes[i].toDouble() - mediaPlayer.audioSpectrumThreshold) * mediaPlayer.volume
                    if(spectrumData.data[i].yValue.toDouble() < newValue) {
                        spectrumData.data[i].yValue = newValue
                    }
                    else {
                        spectrumData.data[i].yValue = spectrumData.data[i].yValue.toDouble() - 0.4
                    }
                }

                spectrumBarChart.lookupAll(".default-color0.chart-bar").forEachIndexed { i, node ->
                    node.style =
                        if(i < numOfBars)
                            "-fx-bar-fill: rgb(${(spectrumData.data[i].yValue.toInt() * 255) / 60}, 0, 255);"
                        else
                            ""
                }
            }

            mediaPlayer.onEndOfMedia = Runnable {
                player.setNextMusic()
            }

            EqualizerHBox.update(player)
        }

        volumeSlider.valueProperty().addListener { _ ->
            volumeSliderDragged()
        }
    }

    fun chooseNewFile() {
        selectedFile = fileChooser.showOpenDialog(primaryStage)
        if (selectedFile != null) {
            var uri = selectedFile!!.toURI().toString()
            addNewFile(uri)
        }
    }

    fun addNewFile(uri: String) {
        player.addNewMusic(Music(uri))
    }

    fun addNewFolder() {
        selectedFile = folderChooser.showDialog(primaryStage)
        Thread {
            if (selectedFile != null) {
                var uri = selectedFile!!.toURI().toString()
                var paths = FolderReader(uri.substringAfter('/').replace("%20", " ")).read()
                addMusicsFromPaths(uri, paths)
            }
        }.start()
    }

    private fun addMusicsFromPaths(uri : String, musicsPaths : ArrayList<String>){
        musicsPaths.forEach {
            var expansion =  it.substringAfterLast('.')
            if(expansion == "mp3" || expansion == "wav") {
                player.addNewMusic(Music((uri + it.substringAfterLast('\\')).replace(" ", "%20")))
            }
        }
    }

    fun settingsShow(){
        SettingsStage(settings).showAndWait()
    }

    fun musicSliderClick(){
        player.seek(Duration((player.getEndTime().toMillis() / 100) * musicSlider.value))
    }

    fun volumeSliderDragged(){
        player.setVolume(volumeSlider.value / 100)
    }

    fun setSelectedMusic(){
        player.setSelectedMusic()
    }

    fun setNextMusic(){
        player.setNextMusic()
    }

    fun setPrevMusic(){
        player.setPrevMusic()
    }

    fun pausePlayer(){
        player.pause()
    }

    fun stopPlayer(){
        player.stop()
    }

//    fun deleteMusicNow(){
//        if(mPlayer?.media == musicSelected?.getMedia()) {
//            mPlayer?.stop()
//            mPlayer?.dispose()
//            mPlayer = null
//            musicSlider.value = 0.0
//        }
//
//        observableList.remove(musicSelected)
//    }

    fun spectrumPageTurnOn(){
//        mainBlock.children.clear()
//        mainBlock.center = spectrumBarChart;
        mainBlock.children.clear()
        val pane = BorderPane()
        pane.center = spectrumBarChart
        pane.top = EqualizerHBox.getBox()
        mainBlock.center = pane;
    }

    fun lyricsPageTurnOn(){
        mainBlock.children.clear()
        mainBlock.center = MusicInfoTextBox.init(player.getMusicNow())
    }

    fun musicTableTurnOn(){
        mainBlock.children.clear()
        mainBlock.center = tableViewMusic;
    }

    fun musicDBTableTurnOn(){
        mainBlock.children.clear()
        mainBlock.center = MusicFromDirectoryTable.init(this);
    }
}
