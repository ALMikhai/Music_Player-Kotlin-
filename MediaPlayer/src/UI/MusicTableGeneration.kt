package UI

import Model
import javafx.scene.control.TableColumn
import Music
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory

class MusicTableGeneration {

    companion object {
        fun init(model: Model) : TableView<Music>{
            val table = TableView(model.player.playList)

            val pathColumn: TableColumn<Music, String> = TableColumn("Name")
            pathColumn.prefWidth = 460.0
            pathColumn.cellValueFactory = PropertyValueFactory<Music, String>("Name")
            table.columns.add(pathColumn)

            val durationColumn: TableColumn<Music, String> = TableColumn("Duration")
            durationColumn.prefWidth = 140.0;
            durationColumn.cellValueFactory = PropertyValueFactory<Music, String>("Duration")
            table.columns.add(durationColumn)

            val musicSelectionModel = table.selectionModel
            musicSelectionModel.selectedItemProperty().addListener { changed, oldValue, newValue ->
                model.player.selectedMusic = newValue
            }

            return table
        }
    }
}