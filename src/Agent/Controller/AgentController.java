package Agent.Controller;

import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import Resources.AlertCreator;
import TravelExperts.Agent.DBClass.DBHelper;
import TravelExperts.Agent.Model.Agent;
import Resources.Validator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseEvent;

import javax.swing.*;


public class AgentController {

    DBHelper dbhelper = null;
    ObservableList<Agent> agents = FXCollections.observableArrayList();
    Agent agent;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;


    @FXML
    private ComboBox<Agent> cmbAgtId;

    @FXML
    private ComboBox<Agent> cmbAgencyId;

    @FXML
    private Button btnInsert;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnUpdate;

    @FXML
    private Button btnDelete;

    @FXML
    private TextField agtSearch;


    @FXML
    private TextField agtFirstName;

    @FXML
    private TextField agtMI;

    @FXML
    private TextField agtLastName;

    @FXML
    private TextField agtPhone;

    @FXML
    private TextField agtEmail;

    @FXML
    private TextField agtPosition;

    @FXML
    private TextField agtAgency;

    @FXML
    private TableView<Agent> tblAgent;

    @FXML
    private TableColumn<Agent, Integer> colId;

    @FXML
    private TableColumn<Agent, String> colFname;

    @FXML
    private TableColumn<Agent, String> colMI;

    @FXML
    private TableColumn<Agent, String> colLname;

    @FXML
    private TableColumn<Agent, String> colPosition;

    @FXML
    private TableColumn<Agent, String> colEmail;

    @FXML
    private TableColumn<Agent, String> colPhone;

    @FXML
    private TableColumn<Agent, Integer> colAgency;

    @FXML
    void TextChanged(InputMethodEvent event) {

    }

    @FXML
    void btnAddClick(MouseEvent event) {

        agtFirstName.clear(); agtLastName.clear(); agtMI.clear();agtEmail.clear();agtPhone.clear();agtPosition.clear();
        cmbAgencyId.setValue(null);cmbAgtId.setValue(null);

    }

    @FXML
    void btnDelete(MouseEvent event) {

        Agent selectedAgentForDelete = cmbAgtId.getSelectionModel().getSelectedItem();
        if(cmbAgtId.getSelectionModel().getSelectedItem() == null){

            AlertCreator.FailedAlert("Please select item to delete");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Deleting Agent");
        alert.setContentText("Are you sure you want to delete this Agent" + selectedAgentForDelete.getAgtFirstName()+" "+selectedAgentForDelete.getAgtLastName());
        Optional<ButtonType> response = alert.showAndWait();

        if(response.get() == ButtonType.OK){
            Boolean result = DBHelper.getInstance().deleteAgent(selectedAgentForDelete);
            if(result){
                AlertCreator.SuccessAlert("Agent" + " " +  selectedAgentForDelete.getAgtLastName() + " " + "has Successfully Deleted");
                agents.remove(selectedAgentForDelete);
                loadAgents();
                fillAgentTable();
            } else { AlertCreator.FailedAlert("Delete operation has been failed, Please try Again!");}
        }else {
            AlertCreator.FailedAlert("Delete operation has been cancelled by user");
        }

    }

    @FXML
    void btnSaveClick(MouseEvent event) {
            addAgent();
    }

    @FXML
    void btnUpdateClick(MouseEvent event) {
  //      Agent selectedAgentForUpdate = cmbAgtId.getSelectionModel().getSelectedItem();
        Agent agent = new Agent(cmbAgtId.getSelectionModel().getSelectedItem().getAgentId(),agtFirstName.toString(),agtMI.toString(),
                agtLastName.toString(),agtPhone.toString(),agtEmail.toString(),agtPosition.toString(),cmbAgencyId.getSelectionModel().getSelectedItem().getAgencyId());

        if (Validator.IsProvided(agtFirstName, "First Name") && Validator.IsProvided(agtLastName, "Last Name")
                && Validator.IsProvided(agtEmail, "Email") && Validator.IsProvided(agtPosition, "Position"))
        {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Update Agent");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure want to update the record for ?" + agent.getAgtFirstName() + " " + agent.getAgtLastName());
            Optional<ButtonType> response = alert.showAndWait();
            try {
                if (response.get() == ButtonType.OK) {
                    Boolean result = DBHelper.getInstance().updateAgent(agent);

                    if (result) {
                        AlertCreator.SuccessAlert("Agent" + " " + agent.getAgtLastName() + " " + "has Successfully Deleted");
                        agents.remove(agent);
                        loadAgents();
                        fillAgentTable();
                    } else {

                        AlertCreator.FailedAlert("Update operation has been failed, Please try Again!");
                    }
                } else {
                    Alert alertCancel = new Alert(Alert.AlertType.INFORMATION);
                    alertCancel.setTitle("Update Cancelled");
                    alertCancel.setHeaderText("Update operation has been cancelled by user");
                    alertCancel.showAndWait();
                }
            }catch (Exception ex) { ex.getLocalizedMessage(); }
        }else AlertCreator.FailedAlert("Please enter all required fileds");


    }
    @FXML
    public  void initialize() {

        DBHelper dbhelper = DBHelper.getInstance();
        dbhelper = DBHelper.getInstance();
        loadAgents();
        fillAgentTable();
        selectionInTableChanged();
        selectionInComboChanged();
    }

    private void searchByAgent() {

        FilteredList<Agent> filteredList = new FilteredList<>(agents, e ->true);
        agtSearch.setOnKeyReleased(e -> {
            agtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredList.setPredicate((Predicate<? super Agent>) agent -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = newValue.toLowerCase();
                    if (agent.getAgtFirstName().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    } else if (agent.getAgtLastName().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }
                    return false;
                });

            });

            SortedList<Agent> sortedList = new SortedList<>(filteredList);
            sortedList.comparatorProperty().bind(tblAgent.comparatorProperty());
            tblAgent.setItems(sortedList);
        });
    }

    public  void loadAgents() {

        tblAgent.setItems(null);
         dbhelper = DBHelper.getInstance();
        //  ObservableList<Agent> agentList= FXCollections.observableArrayList();
        String query = "SELECT * FROM agents ORDER BY agtLastName ASC";
        ResultSet rs = dbhelper.executeQuery(query);
        try {

            while (rs.next()) {
                int id =  rs.getInt(1);
                String agtFname =  rs.getString(2);
                String agtM = rs.getString(3);
                String agtLname = rs.getString(4);
                String agtPhone = rs.getString(5);
                String agtEmail = rs.getString(6);
                String agtPos = rs.getString(7);
                int cmbAgencyId = rs.getInt(8);

                agents.add(new Agent(id,agtFname,agtM,agtLname,agtPhone,agtEmail,agtPos,cmbAgencyId));
            }
        }catch (SQLException ex){
            JOptionPane.showMessageDialog(null,"Errror:" + ex.getMessage(),"Error Occured" ,JOptionPane.ERROR_MESSAGE);
        }
        tblAgent.setItems(agents);
        cmbAgtId.setItems(agents);
    }

    private void fillAgentTable()
    {
        colId.setCellValueFactory(cellData -> cellData.getValue().agentIdProperty().asObject());
        colFname.setCellValueFactory(cellData -> cellData.getValue().agtFirstNameProperty());
        colMI.setCellValueFactory((cellData -> cellData.getValue().agtMiddleInitalProperty()));
        colLname.setCellValueFactory(cellData -> cellData.getValue().agtLastNameProperty());
        colPhone.setCellValueFactory((cellData -> cellData.getValue().agtBusPhoneProperty()));
        colEmail.setCellValueFactory((cellData -> cellData.getValue().agtEmailProperty()));
        colPosition.setCellValueFactory(cellData -> cellData.getValue().agtPositionProperty());
        colAgency.setCellValueFactory((cellData -> cellData.getValue().agencyIdProperty().asObject()));

    }

    private void selectionInTableChanged() {

        //   if(!tblAgent.getSelectionModel().isEmpty()) {

        tblAgent.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Agent>() {
            @Override
            public void changed(ObservableValue<? extends Agent> observable, Agent oldValue, Agent newValue) {

                if (newValue != null) {
                    //     int agentId = cmbAgtId.getSelectionModel().getSelectedIndex();
                    for (Agent agent : agents) {
                        if (agent.getAgentId() == newValue.getAgentId()) {
                            cmbAgtId.getSelectionModel().select(agent);
                            cmbAgencyId.getSelectionModel().select(agent);
                        }
                    }
                    agtFirstName.setText(newValue.getAgtFirstName());
                    agtMI.setText(newValue.getAgtMiddleInital());
                    agtLastName.setText(newValue.getAgtLastName());
                    agtPhone.setText(newValue.getAgtBusPhone());
                    agtEmail.setText(newValue.getAgtEmail());
                    agtPosition.setText(newValue.getAgtPosition());
                }
            }
        });
        // }
    }

    private void selectionInComboChanged() {

        //    if (!cmbAgtId.getSelectionModel().isEmpty()) {
        cmbAgtId.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Agent>() {
            @Override
            public void changed(ObservableValue<? extends Agent> observable, Agent oldValue, Agent newValue) {
                agtFirstName.setText(newValue.getAgtFirstName());
                agtMI.setText(newValue.getAgtMiddleInital());
                agtLastName.setText(newValue.getAgtLastName());
                agtPhone.setText(newValue.getAgtBusPhone());
                agtEmail.setText(newValue.getAgtEmail());
                agtPosition.setText(newValue.getAgtPosition());
                cmbAgencyId.getSelectionModel().getSelectedItem();
            }
        });

    }

    private void addAgent(){

        DBHelper dbhelper = DBHelper.getInstance();


        if(Validator.IsProvided(agtFirstName,"First Name") && Validator.IsProvided(agtLastName,"Last Name")
                && Validator.IsProvided(agtEmail,"Email") && Validator.IsProvided(agtPosition,"Position"))

        {
            //     int agentId = cmbAgtId.getSelectionModel().getSelectedIndex(); auto-increment in database.
            String agtFname = agtFirstName.getText();
            String agtM = agtMI.getText();
            String agtLname = agtLastName.getText();
            String email = agtEmail.getText();
            String phone = agtPhone.getText();
            String position = agtPosition.getText();
            int agency = cmbAgtId.getSelectionModel().getSelectedItem().getAgencyId();

            String query = "INSERT INTO agents VALUES ( "+
                    "'" + agtFname + "'," +
                    "'" + agtM + "'," +
                    "'" + agtLname + "'," +
                    "'" + phone + "'," +
                    "'" + email + "'," +
                    "'" + position + "'," +
                    "'" + agency + "'" +
                    ")";
            System.out.print(query);
            try{
                if(dbhelper.execNonQuery(query)){
                    JOptionPane.showMessageDialog(null,"Insert succeeded");

                } else {
                    JOptionPane.showMessageDialog(null,"Insert Failed");
                }
            }catch (SQLException ex){
                ex.printStackTrace();
            }
        }
    }






}


