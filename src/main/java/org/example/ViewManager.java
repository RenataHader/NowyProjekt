package org.example;

import javafx.scene.Parent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class ViewManager {

    private static ViewManager instance;
    private Stage window;



    private static class ViewData<T> {
        final Parent view;
        final Scene scene;
        final T controller;

        ViewData(Parent view, Scene scene, T controller) {
            this.view = view;
            this.controller = controller;
            this.scene = scene;
        }
    }

    private final Map<String, ViewData<?>> views = new HashMap<>();

    private ViewManager() {
        loadView("main", "/GameSelectionView.fxml", GameSelectionController.class);
        loadView("nickInput", "/NickInputView.fxml", NickInputController.class);
        loadView("charadesDraw", "/CharadesDraw.fxml", CharadesDrawController.class);
        loadView("charadesGuess", "/CharadesGuess.fxml", CharadesGuessController.class);
        loadView("memory", "/MemoryView.fxml", MemoryController.class);
        loadView("endGame", "/EndGameView.fxml", EndGameController.class);
    }

    public static synchronized ViewManager getInstance() {
        if (instance == null) {
            instance = new ViewManager();
        }
        return instance;
    }

    private <T> void loadView(String key, String fxmlPath, Class<T> controllerClass) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            T controller = loader.getController();
            Scene scene = new Scene(view);
            views.put(key,new ViewData<>(view, scene, controller));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML: " + fxmlPath, e);
        }
    }

    public <T> T getController(String key, Class<T> type) {
        ViewData<?> data = views.get(key);
        if (data == null) throw new IllegalArgumentException("No view with key: " + key);
        return type.cast(data.controller);
    }

    public Parent getView(String key) {
        ViewData<?> data = views.get(key);
        if (data == null) throw new IllegalArgumentException("No view with key: " + key);
        return data.view;
    }

    public void setWindow(Stage window){
        this.window = window;
    }


    public void changeView(String key){
        ViewData<?> data = views.get(key);
        if (data == null) throw new IllegalArgumentException("No view with key: " + key);

        this.window.setScene(data.scene);
        this.window.show();
    }

}
