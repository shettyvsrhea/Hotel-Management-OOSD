package application;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Node; 
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;

/** CONCEPT: ENUMS */
enum RoomType { SINGLE, DOUBLE, DELUXE, SUITE }

public class HotelManagementApp extends Application {

    /** CONCEPT: SERIALIZATION */
    public static class Room implements Serializable {
        private static final long serialVersionUID = 1L;
        private final int roomNumber;
        private final RoomType type; 
        private final double price;
        private String status;
        private String guestName;
        private LocalDate checkInDate; // Added for Date-wise logic

        public Room(int roomNumber, RoomType type, double price) {
            this.roomNumber = roomNumber;
            this.type = type;
            this.price = price;
            this.status = "Available";
            this.guestName = "-";
        }

        public int getRoomNumber() { return roomNumber; }
        public RoomType getType() { return type; }
        public double getPrice() { return price; }
        public String getStatus() { return status; }
        public String getGuestName() { return guestName; }
        public LocalDate getCheckInDate() { return checkInDate; }
        public void setStatus(String s) { this.status = s; }
        public void setGuestName(String g) { this.guestName = g; }
        public void setCheckInDate(LocalDate d) { this.checkInDate = d; }
    }

    private final String DATA_FILE = "hotel_data.dat";
    private final String LOG_FILE = "transactions.txt";
    
    /** CONCEPT: GENERICS & COLLECTIONS */
    private ObservableList<Room> masterList = FXCollections.observableArrayList();
    private ArrayList<VBox> allRowContainers = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        loadDataFromFile();
        
        /** CONCEPT: MULTITHREADING */
        Thread autoSave = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000); 
                    saveDataToFile();
                } catch (InterruptedException e) { break; }
            }
        });
        autoSave.setDaemon(true);
        autoSave.start();

        TabPane root = new TabPane();
        root.setStyle("-fx-background-color: #2c3e50;"); // Slate Background

        Tab roomTab = new Tab(" ⚙ Inventory ", createTabContent("Room Management", createRoomForm()));
        Tab checkInTab = new Tab(" 🔑 Check-In ", createTabContent("Guest Entry", createCheckInForm()));
        Tab billingTab = new Tab(" 💰 Billing ", createTabContent("Checkout & Payments", createBillingForm()));
        
        for(Tab t : new Tab[]{roomTab, checkInTab, billingTab}) t.setClosable(false);
        root.getTabs().addAll(roomTab, checkInTab, billingTab);

        Scene scene = new Scene(root, 1150, 850);
        stage.setTitle("Grand Elite Row Management v7.0");
        stage.setOnCloseRequest(e -> saveDataToFile());
        stage.setScene(scene);
        stage.show();
        
        refreshAllGrids();
    }

    /** CONCEPT: SYNCHRONIZATION */
    private synchronized void saveDataToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(new ArrayList<>(masterList));
        } catch (IOException e) { System.err.println("Save failed."); }
    }

    /** CONCEPT: RANDOM ACCESS FILES */
    private void logTransaction(String data) {
        try (RandomAccessFile raf = new RandomAccessFile(LOG_FILE, "rw")) {
            raf.seek(raf.length()); 
            raf.writeBytes(data + "\r\n");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private VBox createTabContent(String titleText, Node form) {
        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 26px; -fx-text-fill: #ecf0f1; -fx-font-weight: bold;");

        VBox rowContainer = new VBox(8); // Vertical stacking for rows
        rowContainer.setPadding(new Insets(10));
        allRowContainers.add(rowContainer); 

        ScrollPane scroll = new ScrollPane(rowContainer);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(450);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: #34495e;");

        VBox layout = new VBox(20, title, scroll, form);
        layout.setPadding(new Insets(30));
        return layout;
    }

    private HBox createRoomRow(Room room) {
    HBox row = new HBox(25); // Increased spacing for the new label
    row.setAlignment(Pos.CENTER_LEFT);
    row.setPadding(new Insets(12, 20, 12, 20));
    
    String accentColor = room.getStatus().equals("Available") ? "#1abc9c" : "#e74c3c";
    row.setStyle("-fx-background-color: #3d566e; -fx-background-radius: 8; -fx-border-color: " + accentColor + "; -fx-border-width: 0 0 0 5; -fx-cursor: hand;");

    // 1. Room Info
    Label lblNum = new Label("ROOM " + room.getRoomNumber());
    lblNum.setPrefWidth(90);
    lblNum.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

    // 2. Type Info
    Label lblType = new Label(room.getType().toString());
    lblType.setPrefWidth(100);
    lblType.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 12px;");

    // 3. Guest Name
    Label lblGuest = new Label(room.getGuestName());
    lblGuest.setPrefWidth(150);
    lblGuest.setStyle("-fx-text-fill: #ecf0f1;");

    // 4. NEW: Check-In Date Label
    String dateText = (room.getStatus().equals("Occupied") && room.getCheckInDate() != null) 
                      ? "In: " + room.getCheckInDate().toString() 
                      : "---";
    Label lblDate = new Label(dateText);
    lblDate.setPrefWidth(120);
    lblDate.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic; -fx-font-size: 12px;");

    // 5. Price Info
    Label lblPrice = new Label("₹" + room.getPrice());
    lblPrice.setPrefWidth(100);
    lblPrice.setStyle("-fx-text-fill: #f1c40f; -fx-font-weight: bold;");

    // 6. Status Info
    Label lblStatus = new Label(room.getStatus().toUpperCase());
    lblStatus.setPrefWidth(100);
    lblStatus.setStyle("-fx-text-fill: " + accentColor + "; -fx-font-weight: bold;");

    // Add everything to the row
    row.getChildren().addAll(lblNum, lblType, lblGuest, lblDate, lblPrice, lblStatus);
    row.setUserData(room);

    // Click behavior (dimming others)
    row.setOnMouseClicked(e -> {
        for (VBox container : allRowContainers) {
            for (Node n : container.getChildren()) n.setOpacity(0.4);
        }
        row.setOpacity(1.0);
    });

    return row;
}

    private GridPane createRoomForm() {
    GridPane form = new GridPane();
    form.setHgap(15); form.setVgap(10);
    form.setPadding(new Insets(20));
    form.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10;");

    TextField txtNum = new TextField(); 
    txtNum.setPromptText("Room #");
    ComboBox<RoomType> cbType = new ComboBox<>(FXCollections.observableArrayList(RoomType.values()));
    cbType.setValue(RoomType.SINGLE);
    TextField txtPrice = new TextField(); 
    txtPrice.setPromptText("Price");
    
    Button btnAdd = new Button("Add Room");
    styleButton(btnAdd, "#27ae60"); // Green for Add

    // NEW: Delete Button
    Button btnDelete = new Button("Delete Selected Room");
    styleButton(btnDelete, "#c0392b"); // Red for Delete

    form.addRow(0, createWhiteLabel("Room #:"), txtNum, createWhiteLabel("Type:"), cbType);
    form.addRow(1, createWhiteLabel("Price:"), txtPrice, btnAdd, btnDelete);

    // ADD LOGIC
    btnAdd.setOnAction(e -> {
        try {
            int rNum = Integer.parseInt(txtNum.getText().trim());
            double rPrice = Double.parseDouble(txtPrice.getText().trim());

            if (rPrice <= 0) { showMsg("Input Error", "Price must be positive."); return; }

            for (Room r : masterList) {
                if (r.getRoomNumber() == rNum) {
                    showMsg("Duplicate Error", "Room #" + rNum + " already exists.");
                    return;
                }
            }

            masterList.add(new Room(rNum, cbType.getValue(), rPrice));
            sortRooms(); refreshAllGrids(); saveDataToFile();
            txtNum.clear(); txtPrice.clear();

        } catch (NumberFormatException ex) {
            showMsg("Format Error", "Enter valid numbers.");
        }
    });

    // DELETE LOGIC
    btnDelete.setOnAction(e -> {
        Room selected = getSelectedRoom(); // Gets the room from the clicked row
        
        if (selected != null) {
            // EXCEPTION: Cannot delete if someone is checked in
            if (selected.getStatus().equals("Occupied")) {
                showMsg("Action Denied", "Cannot delete an occupied room. Process checkout first.");
            } else {
                // Confirmation Dialog
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete Room " + selected.getRoomNumber() + "?", ButtonType.YES, ButtonType.NO);
                confirm.showAndWait();
                
                if (confirm.getResult() == ButtonType.YES) {
                    masterList.remove(selected);
                    refreshAllGrids();
                    saveDataToFile();
                }
            }
        } else {
            showMsg("Selection Error", "Please click on a room row to select it first.");
        }
    });

    return form;
}

    private VBox createCheckInForm() {
    VBox form = new VBox(10);
    form.setPadding(new Insets(20));
    form.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10;");
    
    TextField txtName = new TextField(); 
    txtName.setPromptText("Enter Guest Name");
    
    DatePicker checkInPicker = new DatePicker(LocalDate.now());
    
    // --- RESTRICT PAST DATES ---
    checkInPicker.setDayCellFactory(picker -> new DateCell() {
        @Override
        public void updateItem(LocalDate date, boolean empty) {
            super.updateItem(date, empty);
            // Disable all dates before Today
            setDisable(empty || date.isBefore(LocalDate.now()));
        }
    });

    Button btnCheckIn = new Button("Process Check-In");
    styleButton(btnCheckIn, "#2980b9");

    btnCheckIn.setOnAction(e -> {
        Room selected = getSelectedRoom();
        String name = txtName.getText().trim();
        if (selected != null && selected.getStatus().equals("Available") && name.matches("^[a-zA-Z\\s]+$")) {
            selected.setGuestName(name);
            selected.setCheckInDate(checkInPicker.getValue());
            selected.setStatus("Occupied");
            refreshAllGrids(); saveDataToFile();
            txtName.clear();
        } else { showMsg("Error", "Select a room and enter a valid name."); }
    });

    form.getChildren().addAll(createWhiteLabel("Guest Name:"), txtName, createWhiteLabel("Check-In Date:"), checkInPicker, btnCheckIn);
    return form;
}

    private VBox createBillingForm() {
    VBox form = new VBox(10);
    form.setPadding(new Insets(20));
    form.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10;");
    
    DatePicker checkOutPicker = new DatePicker(LocalDate.now().plusDays(1));


    Button btnBill = new Button("Generate Invoice");
    styleButton(btnBill, "#f39c12");

    btnBill.setOnAction(e -> {
    Room selected = getSelectedRoom();
    
    if (selected != null && selected.getStatus().equals("Occupied")) {
        LocalDate checkIn = selected.getCheckInDate();
        LocalDate checkOut = checkOutPicker.getValue();

        // 1. LOGIC CHECK: Is Check-out before or on the Check-in date?
        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            showMsg("Invalid Date", "Check-out date must be AFTER the Check-in date.\n" +
                    "Guest Checked in on: " + checkIn);
            return; // Stop the code here
        }

        // 2. CALCULATE DURATION
        /** CONCEPT: WRAPPER CLASSES & AUTOBOXING */
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        Double total = nights * selected.getPrice(); 

        // 3. SUCCESS: Show Invoice and Log
        showReceipt(selected, (int)nights, total);
        logTransaction("Room " + selected.getRoomNumber() +"  |  Guest: "+selected.getGuestName()+ "  |  checked out. Nights: " + nights);
        
        // 4. RESET ROOM STATUS
        selected.setStatus("Available");
        selected.setGuestName("-");
        selected.setCheckInDate(null); // Clear the date for next guest
        
        refreshAllGrids(); 
        saveDataToFile();
    } else {
        showMsg("Selection Error", "Please select an 'Occupied' (Red) room row.");
    }
});

    form.getChildren().addAll(createWhiteLabel("Check-Out Date:"), checkOutPicker, btnBill);
    return form;
}

    private void showReceipt(Room room, int days, double total) {
        Alert receipt = new Alert(Alert.AlertType.INFORMATION);
        receipt.setTitle("Official Invoice");
        receipt.setHeaderText(null);
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-border-color: #2c3e50; -fx-border-width: 2;");
        
        Label h = new Label("HOTEL INN - MANIPAL");
        h.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        
        Label details = new Label(
            "Guest: " + room.getGuestName() + "\n" +
            "Room: " + room.getRoomNumber() + " (" + room.getType() + ")\n" +
            "Dates: " + room.getCheckInDate() + " to " + LocalDate.now() + "\n" +
            "Price per Night: " + room.getPrice() + "\n" +
            "Nights: " + days + "\n" +
            "--------------------------\n" +
            "TOTAL: ₹" + String.format("%.2f", total)
        );
        details.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px;");
        
        content.getChildren().addAll(h, details);
        receipt.getDialogPane().setContent(content);
        receipt.showAndWait();
    }

    private Room getSelectedRoom() {
        for (VBox container : allRowContainers) {
            for (Node n : container.getChildren()) {
                if (n.getOpacity() == 1.0) return (Room) n.getUserData();
            }
        }
        return null;
    }

    private void refreshAllGrids() {
        for (VBox container : allRowContainers) {
            container.getChildren().clear();
            for (Room r : masterList) container.getChildren().add(createRoomRow(r));
        }
    }

    private void sortRooms() { masterList.sort(Comparator.comparingInt(Room::getRoomNumber)); }
    private Label createWhiteLabel(String text) { Label l = new Label(text); l.setStyle("-fx-text-fill: white;"); return l; }
    private void styleButton(Button btn, String color) {
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
    }
    private void showMsg(String t, String c) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setContentText(c); a.showAndWait(); }

    private void loadDataFromFile() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            masterList.setAll((ArrayList<Room>) ois.readObject());
        } catch (Exception e) { }
    }

    public static void main(String[] args) { launch(args); }
}