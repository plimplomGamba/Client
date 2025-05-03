package de.plimplom.addonreader.view;

import de.plimplom.addonreader.config.DependencyInjector;
import de.plimplom.addonreader.event.EventBus;
import io.github.palexdev.mfxcore.controls.Label;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

public abstract class BaseController {
    protected final EventBus eventBus = DependencyInjector.getService(EventBus.class);

    protected Stage getStageFromNode(Node node) {
        if (node == null) {
            return null;
        }

        Scene scene = node.getScene();
        if (scene == null) {
            return null;
        }

        Window window = scene.getWindow();
        if (window instanceof Stage) {
            return (Stage) window;
        }

        return null;
    }

    protected void showErrorDialog(VBox parent, String headerText, String content) {
        parent.getChildren().clear();

        VBox vBox = new VBox();
        Label label = new Label(headerText);
        label.setStyle("-fx-text-fill: white");
        Label label2 = new Label(content);
        label2.setStyle("-fx-text-fill: white");
        label2.setWrapText(true);

        vBox.getChildren().addAll(label, label2);

        parent.getChildren().add(vBox);
//        Platform.runLater(() -> {
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setTitle(title);
//            alert.setHeaderText(headerText);
//            alert.setContentText(content);
//
//            if (ex != null) {
//                StringWriter sw = new StringWriter();
//                PrintWriter pw = new PrintWriter(sw);
//                ex.printStackTrace(pw);
//
//                TextArea textArea = new TextArea(sw.toString());
//                textArea.setEditable(false);
//                textArea.setWrapText(true);
//                textArea.setMaxWidth(Double.MAX_VALUE);
//                textArea.setMaxHeight(Double.MAX_VALUE);
//
//                GridPane.setVgrow(textArea, Priority.ALWAYS);
//                GridPane.setHgrow(textArea, Priority.ALWAYS);
//
//                GridPane expContent = new GridPane();
//                expContent.setMaxWidth(Double.MAX_VALUE);
//                expContent.add(textArea, 0, 1);
//
//                alert.getDialogPane().setExpandableContent(expContent);
//            }
//
//            alert.showAndWait();
//        });
    }

    protected void showInfoDialog(VBox parent, String title, String headerText, String content) {
        parent.getChildren().clear();
//        Platform.runLater(() -> {
//            Alert alert = new Alert(Alert.AlertType.INFORMATION);
//            alert.setTitle(title);
//            alert.setHeaderText(headerText);
//            alert.setContentText(content);
//            alert.showAndWait();
//        });
    }
}
