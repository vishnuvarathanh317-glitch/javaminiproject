package com.hostelmanagement.ui;

import com.hostelmanagement.enums.RoomType;
import com.hostelmanagement.model.Block;
import com.hostelmanagement.model.Floor;
import com.hostelmanagement.model.Hostel;
import com.hostelmanagement.model.Room;
import com.hostelmanagement.service.HostelService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Hostel & Room Infrastructure Management Screen (Module C).
 * Manages Hostels, Blocks, Floors, and Rooms with automatic bed provisioning.
 */
public class HostelManagementView {

    private final VBox root;
    private final HostelService hostelService = new HostelService();
    private final TableView<Room> roomTable = new TableView<>();
    private final ObservableList<Room> roomList = FXCollections.observableArrayList();

    public HostelManagementView() {
        root = new VBox(16);
        root.setPadding(new Insets(10));

        // ── TOP ACTION BAR ──
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label header = new Label("Room & Bed Inventory");
        header.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addHostelBtn = new Button("+ Add Hostel");
        addHostelBtn.getStyleClass().add("btn-secondary");
        addHostelBtn.setOnAction(e -> showAddHostelDialog());

        Button addBlockBtn = new Button("+ Add Block");
        addBlockBtn.getStyleClass().add("btn-secondary");
        addBlockBtn.setOnAction(e -> showAddBlockDialog());

        Button addRoomBtn = new Button("+ Add Room & Provision Beds");
        addRoomBtn.getStyleClass().add("btn-primary");
        addRoomBtn.setOnAction(e -> showAddRoomDialog());

        topBar.getChildren().addAll(header, spacer, addHostelBtn, addBlockBtn, addRoomBtn);

        // ── TABLE ──
        TableColumn<Room, String> blockCol = new TableColumn<>("Block");
        blockCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBlockName()));
        blockCol.setPrefWidth(180);

        TableColumn<Room, String> floorCol = new TableColumn<>("Floor");
        floorCol.setCellValueFactory(d -> new SimpleStringProperty("Floor " + d.getValue().getFloorNumber()));
        floorCol.setPrefWidth(100);

        TableColumn<Room, String> roomCol = new TableColumn<>("Room Number");
        roomCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRoomNumber()));
        roomCol.setPrefWidth(130);

        TableColumn<Room, String> typeCol = new TableColumn<>("Room Type");
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRoomType().name()));
        typeCol.setPrefWidth(140);

        TableColumn<Room, String> capCol = new TableColumn<>("Bed Capacity");
        capCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCapacity() + " Beds"));
        capCol.setPrefWidth(120);

        TableColumn<Room, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isActive() ? "Active" : "Inactive"));
        statusCol.setPrefWidth(100);

        roomTable.getColumns().addAll(blockCol, floorCol, roomCol, typeCol, capCol, statusCol);
        roomTable.setItems(roomList);
        roomTable.setPlaceholder(new Label("No rooms configured yet. Click '+ Add Room' to begin provisioning."));
        VBox.setVgrow(roomTable, Priority.ALWAYS);

        root.getChildren().addAll(topBar, roomTable);
        loadRooms();
    }

    private void loadRooms() {
        roomList.clear();
        try {
            List<Room> rooms = hostelService.getAllRooms();
            roomList.addAll(rooms);
        } catch (Exception ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load rooms", ex.getMessage());
        }
    }

    private void showAddHostelDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Hostel Facility");
        dialog.setHeaderText("Create a new residential campus hostel.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(16));
        TextField nameField = new TextField();
        TextField addressField = new TextField();

        grid.add(new Label("Hostel Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Campus Address:"), 0, 1); grid.add(addressField, 1, 1);
        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    hostelService.createHostel(nameField.getText(), addressField.getText());
                    ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Success", "Hostel Created", "Hostel registered successfully.");
                } catch (Exception ex) {
                    ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "Could not create hostel", ex.getMessage());
                }
            }
        });
    }

    private void showAddBlockDialog() {
        List<Hostel> hostels = hostelService.getAllHostels();
        if (hostels.isEmpty()) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "Warning", "No Hostel Exists", "Please create a Hostel first.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Hostel Block");
        dialog.setHeaderText("Create a residential wing or block.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(16));

        ComboBox<Hostel> hostelBox = new ComboBox<>(FXCollections.observableArrayList(hostels));
        hostelBox.setValue(hostels.get(0));
        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Block C (Kaveri)");

        grid.add(new Label("Parent Hostel:"), 0, 0); grid.add(hostelBox, 1, 0);
        grid.add(new Label("Block Name:"), 0, 1); grid.add(nameField, 1, 1);
        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    Block block = hostelService.createBlock(hostelBox.getValue().getId(), nameField.getText());
                    // Automatically add Ground and 1st floor
                    hostelService.createFloor(block.getId(), 0);
                    hostelService.createFloor(block.getId(), 1);
                    ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Success", "Block Created",
                            "Block created with Ground & 1st floor initialized.");
                } catch (Exception ex) {
                    ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "Could not create block", ex.getMessage());
                }
            }
        });
    }

    private void showAddRoomDialog() {
        List<Block> blocks = hostelService.getAllBlocks();
        if (blocks.isEmpty()) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "Warning", "No Block Exists", "Please create a Block first.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Room & Provision Beds");
        dialog.setHeaderText("Adding a room will automatically create atomic bed slots for its capacity.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(16));

        ComboBox<Block> blockBox = new ComboBox<>(FXCollections.observableArrayList(blocks));
        blockBox.setValue(blocks.get(0));

        ComboBox<Integer> floorBox = new ComboBox<>(FXCollections.observableArrayList(0, 1, 2, 3));
        floorBox.setValue(0);

        TextField roomNoField = new TextField();
        roomNoField.setPromptText("e.g. C-101");

        ComboBox<RoomType> typeBox = new ComboBox<>(FXCollections.observableArrayList(RoomType.values()));
        typeBox.setValue(RoomType.DOUBLE);

        Spinner<Integer> capSpinner = new Spinner<>(1, 6, 2);

        typeBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == RoomType.SINGLE) capSpinner.getValueFactory().setValue(1);
            else if (newVal == RoomType.DOUBLE) capSpinner.getValueFactory().setValue(2);
            else if (newVal == RoomType.TRIPLE) capSpinner.getValueFactory().setValue(3);
            else if (newVal == RoomType.DORMITORY) capSpinner.getValueFactory().setValue(6);
        });

        grid.add(new Label("Block:"), 0, 0); grid.add(blockBox, 1, 0);
        grid.add(new Label("Floor:"), 0, 1); grid.add(floorBox, 1, 1);
        grid.add(new Label("Room Number:"), 0, 2); grid.add(roomNoField, 1, 2);
        grid.add(new Label("Room Type:"), 0, 3); grid.add(typeBox, 1, 3);
        grid.add(new Label("Bed Capacity:"), 0, 4); grid.add(capSpinner, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    // Check or create floor
                    int blockId = blockBox.getValue().getId();
                    int floorNum = floorBox.getValue();
                    List<Floor> floors = hostelService.getFloorsByBlock(blockId);
                    Floor targetFloor = null;
                    for (Floor f : floors) {
                        if (f.getFloorNumber() == floorNum) {
                            targetFloor = f;
                            break;
                        }
                    }
                    if (targetFloor == null) {
                        targetFloor = hostelService.createFloor(blockId, floorNum);
                    }

                    hostelService.createRoom(targetFloor.getId(), roomNoField.getText(), typeBox.getValue(), capSpinner.getValue());
                    loadRooms();
                    ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Success", "Room & Beds Provisioned",
                            "Room " + roomNoField.getText() + " and " + capSpinner.getValue() + " atomic beds created.");
                } catch (Exception ex) {
                    ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", "Could not create room", ex.getMessage());
                }
            }
        });
    }

    public VBox getView() {
        return root;
    }
}
