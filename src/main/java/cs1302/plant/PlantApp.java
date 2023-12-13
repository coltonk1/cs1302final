package cs1302.plant;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.net.URI;
import java.net.URLEncoder;

import java.util.Collection;
import java.util.Map;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import cs1302.plant.WikiResponse;
import cs1302.plant.WikiPages;
import cs1302.plant.WikiQuery;
import cs1302.plant.TrefleResponse;
import cs1302.plant.TrefleDetailedResponse;
import cs1302.plant.TrefleDetailedData;
import cs1302.plant.TrefleDistributionData;

/**
 * App to display information about inputted plants,
 * and also to display specific information such as
 * where the plant is native to.
 */
public class PlantApp extends Application {

    /** HTTP client. */
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)           // uses HTTP protocol version 2 where possible
        .followRedirects(HttpClient.Redirect.NORMAL)  // always redirects, except from HTTPS to HTTP
        .build();                                     // builds and returns a HttpClient object

    /** Google {@code Gson} object for parsing JSON-formatted strings. */
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()                          // enable nice output when printing
        .create();                                    // builds and returns a Gson object

    private Stage stage;
    private Scene scene;
    private VBox root;

    private Label apiStatus;

    private Text descText;
    private TextFlow description;
    private Label title;
    private ImageView mainImage;
    private TextField query;
    private VBox dataSection;
    private Button search;

    /**
     * Initializes instance variables.
     */
    public PlantApp() {
        // Init all of the necessary components.
        this.stage = null;
        this.scene = null;
        this.root = new VBox();
        this.apiStatus = new Label("Enter common name of plant, or for more specific " +
                                   "searches, please enter the scientific name.");
        this.description = new TextFlow();
        this.title = new Label();
        this.mainImage = new ImageView();
        this.descText = new Text();
        this.query = new TextField();
        this.dataSection = new VBox();
    } // PlantApp


    // Wikipedia API
    private String wikiURL = "https://en.wikipedia.org/w/api.php?" +
        "action=query&redirects=true&format=json&prop=extracts&exintro=true&titles=";

    // Trefle access token
    private String trefleToken = "BT6pSX-_niSbGo_N2UnNiu0m58AJ_J_KbwEc2kedz1Y";

    // Trefle API
    private String trefleURL = "https://trefle.io/api/v1/plants/search?token=" +
        trefleToken + "&q=";

    // Trefle Detailed API
    private String trefleDetailedURL = "https://trefle.io/api/v1/plants/";

    /**
     * Used to display errors.
     * @param errMessage Message to display in error box.
     */
    private void displayError(String errMessage) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setContentText(errMessage);
            alert.showAndWait();
        });
    } // displayError

    /**
     * Used to call the trefle and wiki api without
     * creating a mess.
     * @param apiURL Which api to call.
     * @param plantName Plant to search for.
     * @param responseClass The class to return and
     * turn into gson.
     * @param <Response> Used for defining what to return
     * and what to use for json parsing.
     * @return Response data.
     */
    private <Response> Response callApi(String apiURL, String plantName,
                                        Class<Response> responseClass) throws RuntimeException {
        String query = apiURL;
        System.out.println(query);
        try {
            query += URLEncoder.encode(plantName, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Create the complete URL and search it.
        final String searchQuery = query;
        System.out.println(searchQuery);
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(searchQuery))
            .build();
        try {
            HttpResponse<String> res = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            String body = res.body();
            // If result's status is not in the 200s then it is not good.
            if (res.statusCode() < 200 || res.statusCode() >= 300) {
                throw new RuntimeException();
            }
            Response response = GSON.fromJson(body, responseClass);
            return response;
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Calls wiki api.
     * @param plantName Plant to get info on.
     * @return Response data.
     */
    private WikiResponse getWikiInfo(String plantName) {
        return this.<WikiResponse>callApi(this.wikiURL, plantName, WikiResponse.class);
    }

    /**
     * Calls trefle api.
     * @param plantName Plant to get info on.
     * @return Response data.
     */
    private TrefleResponse getTrefleInfo(String plantName) {
        return this.<TrefleResponse>callApi(this.trefleURL, plantName, TrefleResponse.class);
    }

    /**
     * Gets more detailed information on the plant.
     * @param id Id of plant to get extra info on.
     * @return Detailed response data.
     */
    private TrefleDetailedData getDetailedTrefleInfo(String id) throws RuntimeException {
        final String searchQuery = this.trefleDetailedURL + id + "?token=" + this.trefleToken;
        System.out.println(searchQuery);
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(searchQuery))
            .build();
        try {
            HttpResponse<String> res = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            String body = res.body();
            // If result's status is not in the 200s then it is not good.
            if (res.statusCode() < 200 || res.statusCode() >= 300) {
                throw new RuntimeException();
            }
            TrefleDetailedResponse response = GSON.fromJson(body, TrefleDetailedResponse.class);
            return response.data.main;
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Removes HTML from the string such as <div> </div>.
     * @param input The string to remove html from.
     * @return Cleaned up String.
     */
    private String removeHTML(String input) {
        return input.replaceAll("\\<.*?\\>", "");
    }

    /**
     * Returns the data that needs additional work.
     * @param tdd The data to go through.
     * @param output The arraylist to add to.
     * @return The complete ArrayList.
     */
    private ArrayList<String> moreData(TrefleDetailedData tdd, ArrayList<String> output) {
        if (tdd.ediblePart != null) {
            String edibleParts = "What you can eat:\n";
            for (String s : tdd.ediblePart) {
                if (s != null) {
                    edibleParts += s + "\n";
                }
            }
            output.add(edibleParts);
        }
        if (tdd.distributions != null) {
            String distributions = "";
            if (tdd.distributions.nativeData != null) {
                distributions = "Where this plant is native:\n";
                for (TrefleDistributionData td : tdd.distributions.nativeData) {
                    if (td.name != null) {
                        distributions += td.name + "\n";
                    }
                }
                output.add(distributions);
            }
            if (tdd.distributions.introduced != null) {
                distributions = "Where this plant was introduced:\n";
                for (TrefleDistributionData td : tdd.distributions.introduced) {
                    if (td.name != null) {
                        distributions += td.name + "\n";
                    }
                }
                output.add(distributions);
            }
        }
        return output;
    }

    /**
     * Only gets values that are not null.
     * @param tdd Trefle data to parse.
     * @return ArrayList of good data.
     */
    private ArrayList<String> getValidData(TrefleDetailedData tdd) {
        if (tdd == null) {
            return new ArrayList<String>();
        }
        ArrayList<String> output = new ArrayList<String>();
        if (tdd.commonName != null) {
            output.add("Common name: " + tdd.commonName);
        }
        output.add("Scientific name: " + tdd.scientificName);
        if (tdd.year != null) {
            output.add("Year of first publication: " + tdd.year);
        }
        if (tdd.bibliography != null) {
            output.add("First publication: " + tdd.bibliography);
        }
        if (tdd.author != null) {
            output.add("Author: " + tdd.author);
        }
        if (tdd.vegetable != null) {
            output.add("Is this vegetable?: " + (tdd.vegetable.booleanValue() ? "Yes." : "No."));
        }
        if (tdd.genus != null) {
            output.add("Genus: " + tdd.genus);
        }
        if (tdd.family != null) {
            output.add("Family: " + tdd.family);
        }
        if (tdd.edible != null) {
            output.add("Is this edible?: " + (tdd.edible.booleanValue() ? "Yes." : "No."));
        }

        output = moreData(tdd, output);
        return output;
    }

    /**
     * Turns given string arraylist into labels in data section.
     * @param strings Strings to turn into labels.
     */
    private void addLabels(ArrayList<String> strings) {
        Platform.runLater(() -> dataSection.getChildren().clear());
        for (String s : strings) {
            Platform.runLater(() -> dataSection.getChildren().add(new Label(s)));
        }
    }

    /**
     * Event after button is clicked, calls APIs and
     * makes sure data is okay.
     */
    private void searchEvent() {
        try {
            Platform.runLater(() -> apiStatus.setText("Searching..."));
            String searchFor = this.query.getText();
            TrefleResponse data = getTrefleInfo(searchFor);
            String commonNameUnfinal = data.data[0].commonName;
            if (commonNameUnfinal == null) {
                commonNameUnfinal = "";
            }
            final String commonName = commonNameUnfinal;

            String imageURL = data.data[0].imageURL;
            Image image;
            if (imageURL != null) {
                image = new Image(data.data[0].imageURL);
            } else {
                image = new Image("https://static-00.iconduck.com/assets.00" +
                                  "/no-image-icon-2048x2048-2t5cx953.png");
            }
            Platform.runLater(() -> apiStatus.setText("Getting extra trefle data..."));
            TrefleDetailedData mainData = getDetailedTrefleInfo(data.data[0].id);
            ArrayList<String> validData = getValidData(mainData);

            Platform.runLater(() -> apiStatus.setText("Getting wiki data..."));
            WikiQuery wikiData = getWikiInfo(data.data[0].scientificName).query;
            Collection<WikiPages> pages = wikiData.pages.values();
            for (WikiPages page : pages) {
                if (page.extract != null) {
                    descText.setText(removeHTML(page.extract).trim());
                } else {
                    descText.setText("No wiki data found.");
                }
                break;
            }
            addLabels(validData);
            Platform.runLater(() -> title.setText(commonName +
                                                " (" + data.data[0].scientificName + ")"));
            mainImage.setImage(image);
            Platform.runLater(() -> apiStatus.setText(searchFor));
            Platform.runLater(() -> this.search.setDisable(false));
        } catch (IndexOutOfBoundsException e) {
            Platform.runLater(() -> apiStatus.setText("Search failed."));
            displayError("No data found. Try again.");
            Platform.runLater(() -> this.search.setDisable(false));
        } catch (RuntimeException e) {
            Platform.runLater(() -> apiStatus.setText("Search failed."));
            displayError("Could not fetch data.");
            Platform.runLater(() -> this.search.setDisable(false));
        }
    }

    /**
     * Starts a new thread after button is clicked.
     */
    private void searchClicked() {
        this.search.setDisable(true);
        Thread thread = new Thread(() -> searchEvent());
        thread.setDaemon(true);
        thread.start();
    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        HBox header = new HBox();
        search = new Button("Search");
        query.setPromptText("Enter the name of any plant here.");
        header.getChildren().addAll(query, search);
        header.setHgrow(query, Priority.ALWAYS);
        header.setSpacing(10);

        Font bold = Font.font("Calibri", FontWeight.BOLD, 14);
        title.setFont(bold);
        title.setPadding(new Insets(5, 5, 5, 5));

        TabPane pane = new TabPane();
        Tab tab1 = new Tab("Description");
        VBox tab1content = new VBox();
        tab1.setContent(tab1content);
        tab1.setClosable(false);

        Tab tab2 = new Tab("Data");
        VBox tab2content = new VBox();
        tab2.setContent(tab2content);
        tab2.setClosable(false);

        pane.getTabs().addAll(tab1, tab2);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(description);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefHeight(300);
        description.setPadding(new Insets(10, 10, 10, 10));

        ScrollPane dataScrollPane = new ScrollPane(dataSection);
        dataScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        dataScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        dataScrollPane.setPrefHeight(300);

        description.getChildren().add(descText);
        description.setPrefWidth(700);
        description.setLineSpacing(5);

        this.root.setSpacing(10);

        VBox imageContainer = new VBox(mainImage);
        imageContainer.setAlignment(Pos.CENTER);

        mainImage.setFitWidth(250);
        mainImage.setFitHeight(200);
        mainImage.setPreserveRatio(true);

        tab1content.getChildren().addAll(title, scrollPane);
        tab2content.getChildren().add(dataScrollPane);
        this.root.getChildren().addAll(header, apiStatus, imageContainer, pane);

        dataSection.setSpacing(10);
        dataScrollPane.setPadding(new Insets(10, 10, 10, 10));

        search.setOnAction(e -> searchClicked());
    } // init

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        this.scene = new Scene(this.root);
        this.stage.setOnCloseRequest(event -> Platform.exit());
        this.stage.setTitle("PlantApp!");
        this.stage.setScene(this.scene);
        this.stage.sizeToScene();
        this.stage.show();
        Platform.runLater(() -> this.stage.setResizable(false));
    } // start

    /** {@inheritDoc} */
    @Override
    public void stop() {
        System.out.println("stop() called");
    } // stop

} // PlantApp
