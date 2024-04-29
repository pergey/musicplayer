package com.example.musicplayer5;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;


import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    private Stage stage;
    private Scene scene;

    @FXML
    private VBox songLayout;

    @FXML
    private TextField tfSearch;



    public void switchToListenScreen(MouseEvent event) throws IOException
    {
        Parent root = FXMLLoader.load(getClass().getResource("listen.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        tfSearch.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                List<Song> songsList;
                try {
                    songsList = getSongList(tfSearch.getText());

                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                try {
                    for (int i = 0; i < songsList.size(); ++i) {
                        FXMLLoader fxmlLoader = new FXMLLoader();
                        fxmlLoader.setLocation(getClass().getResource("song.fxml"));
                        HBox songBox = fxmlLoader.load();
                        SongItemController songItemController = fxmlLoader.getController();

                        songItemController.setSong(songsList.get(i));
                        //Do this to change screen when we click on music
                        songBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
                            public void handle(MouseEvent mouseEvent) {
                                try {


                                    switchToListenScreen(mouseEvent);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        songLayout.getChildren().add(songBox);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }


    private ArrayList getSongList(String search) throws ParseException
    {
        String response = Utilities.makeHttpRequest("http://localhost/player/api/v1/songs/search?q=" + search);
        Gson gson = new Gson();
        ArrayList songs = gson.fromJson(response, new TypeToken<List<Song>>() {}.getType());
        return songs;
    }

}