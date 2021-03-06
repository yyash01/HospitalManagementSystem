/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ehospitaldrugs;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.events.JFXDialogEvent;
import ehospitaldb.EHospitalDB;
import ehospitaldialog.EHospitalDialog;
import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import impl.org.controlsfx.autocompletion.SuggestionProvider;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javax.imageio.ImageIO;

/**
 *
 * @author Andre Kelvin
 */
public class EHospitalDrugController implements Initializable {
    
    @FXML
    private StackPane stackPane;
    @FXML
    private JFXTextField textSearch;
    @FXML
    private ImageView imageView;
    @FXML
    private JFXButton buttonEdit,buttonDelete;
    @FXML
    private TableView<Drug> tableView;
    @FXML
    private TableColumn columnID,columnName,columnDescrip,columnCate,columnQty,columnPrice,columnManufac;
    private PreparedStatement ps;
    private ResultSet rs;
    private ObservableList obList;
    private Image defaultImage,image;
    private BufferedImage buff;
    private File drugImageFile;
    private Drug selectedDrug;
    private List searchList;
    private SuggestionProvider suggestions;
    private FXMLLoader loaderAdd, loaderEdit, loaderDelete, loaderCate;
    private Parent rootAdd, rootEdit, rootDelete, rootCate;
    private JFXDialog dialogCate;
    private AddDrugController add;
    private EditDrugController edit;
    private DeleteDrugController del;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        try {
            new File(System.getProperty("user.home")+File.separator+"DrugImage").mkdir();
            
            obList=FXCollections.observableArrayList();
            tableView.setItems(obList);
            
            defaultImage=imageView.getImage();
            
            searchList = new ArrayList();
            suggestions = SuggestionProvider.create(searchList);
            
            AutoCompletionTextFieldBinding autoBinding = new AutoCompletionTextFieldBinding<>(textSearch, suggestions);

            columnID.setCellValueFactory(new PropertyValueFactory("id"));
            columnName.setCellValueFactory(new PropertyValueFactory("name"));
            columnDescrip.setCellValueFactory(new PropertyValueFactory("descrip"));
            columnCate.setCellValueFactory(new PropertyValueFactory("cate"));
            columnQty.setCellValueFactory(new PropertyValueFactory("qty"));
            columnPrice.setCellValueFactory(new PropertyValueFactory("price"));
            columnManufac.setCellValueFactory(new PropertyValueFactory("manufac"));
            
            buttonDelete.setDisable(true);
            buttonEdit.setDisable(true);
            
            //Display Image When Drug is Selected
            tableView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Drug> observable, Drug oldValue, Drug newValue) -> {
                try {
                    selectedDrug=newValue;
                
                    ps=EHospitalDB.getCon().prepareStatement("Select PHOTO From DRUG Where NAME=?");
                    ps.setString(1, selectedDrug.getName());
                    rs=ps.executeQuery();
                
                    if (rs.next()) {
                        drugImageFile=new File(rs.getString("PHOTO"));
                        buff=ImageIO.read(drugImageFile);
                        image=SwingFXUtils.toFXImage(buff, null);
                        imageView.setImage(image);
                    }
                    
                    buttonEdit.setDisable(false);
                    buttonDelete.setDisable(false);
                    textSearch.clear();
                    
                } catch (Exception e) {
                }
            });
            
            //Select the Searched Drug
            textSearch.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                tableView.getItems().stream()
                        .filter(item -> (item.getName() == null ? newValue == null : item.getName().equals(newValue)))
                        .findAny().ifPresent(item -> {
                            tableView.getSelectionModel().select(item);
                            //tableView.scrollTo(item);
                        });
            });
            
            //tableView.getFocusModel().focus(-1);
        } catch (Exception e) {
        }
    }  
    
    public void populateTable() {
        try {
            obList.clear();
            ps=EHospitalDB.getCon().prepareStatement("Select * From DRUG");
            rs=ps.executeQuery();
            
            while(rs.next()){
                obList.add(new Drug(rs.getInt("ID"), rs.getString("Name"), rs.getString("DESCRIPTION"), rs.getString("CATEGORY"), rs.getInt("QUANTITY"),
                        rs.getInt("PRICE"), rs.getString("MANUFACTURER")));
            }
        } catch (Exception e) {
        }
    }
    
    //Populate Search Auto Complete Textfield with Drug Names
    public void searchDrug(){
        try {
            searchList.clear();
            ps = EHospitalDB.getCon().prepareStatement("Select NAME From DRUG");
            rs = ps.executeQuery();

            while (rs.next()) {
                searchList.add(rs.getString("NAME"));
            }
            ps.close();

            suggestions.clearSuggestions();
            suggestions.addPossibleSuggestions(searchList);
        } catch (Exception e) {
        }
    }
    
    @FXML
    private void categoryAction() {
        try {
            if (loaderCate == null) {
                loaderCate = new FXMLLoader(getClass().getResource("Category.fxml"));
                rootCate = loaderCate.load();
                
                dialogCate=new JFXDialog(stackPane, (Region)rootCate, JFXDialog.DialogTransition.TOP);
            }
            dialogCate.show();
        } catch (Exception e) {
        }
    }
    
    @FXML
    private void addAction() {
        try {
            if (loaderAdd == null) {
                loaderAdd = new FXMLLoader(getClass().getResource("AddDrug.fxml"));
                rootAdd = loaderAdd.load();
                
                add=loaderAdd.getController();
                add.setObList(obList);
            }
            EHospitalDialog.showDialog(stackPane, rootAdd);
            add.populateCategoryCombo();
            
            EHospitalDialog.dialog.setOnDialogClosed((JFXDialogEvent event) -> {
                searchDrug();
            });
        } catch (Exception e) {
        }
    }
    
    @FXML
    private void editAction() {
        try {
            if (loaderEdit == null) {
                loaderEdit = new FXMLLoader(getClass().getResource("EditDrug.fxml"));
                rootEdit = loaderEdit.load();
                
                edit=loaderEdit.getController();
                edit.setImageView(imageView);
            }
            edit.setSelectedDrug(selectedDrug);
            edit.populateCategoryCombo();
            
            EHospitalDialog.showDialog(stackPane, rootEdit);
            
            EHospitalDialog.dialog.setOnDialogClosed((JFXDialogEvent event) -> {
                searchDrug();
                
                //tableView.getSelectionModel().clearSelection();
                
                buttonDelete.setDisable(true);
                buttonEdit.setDisable(true);
            });
        } catch (Exception e) {
        }
    }
    
    @FXML
    private void deleteAction() {
        try {
            if (loaderDelete == null) {
                loaderDelete = new FXMLLoader(getClass().getResource("DeleteDrug.fxml"));
                rootDelete = loaderDelete.load();
                
                del=loaderDelete.getController();
            }
            
            del.setSelectedDrug(selectedDrug);
            del.setDrugImageFile(drugImageFile);
            
            EHospitalDialog.showDialog(stackPane, rootDelete);
            
            EHospitalDialog.dialog.setOnDialogClosed((JFXDialogEvent event) -> {
                searchDrug();
                populateTable();
                
                imageView.setImage(defaultImage);
                
                buttonDelete.setDisable(true);
                buttonEdit.setDisable(true);
                //tableView.getFocusModel().focus(-1);
            });
        } catch (Exception e) {
        }
    }
    
}
