package aydin.firebasedemospring2024;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginRegisterPage {

    @FXML
    TextField emailField;

    @FXML
    PasswordField passwordField;

    @FXML
    Label errorText;

    private String CurrentUser;

    public void handleRegisterButtonClick(){
        if(registerUser()){
            addUserToDB();
            switchToDataAccessView();
        }
        errorText.setVisible(true);
    }

    public void handleSignInButtonClick(){
        if(authenticateUser(emailField.getText(), passwordField.getText())){
            switchToDataAccessView();
        }
        errorText.setVisible(true);
    }

    @FXML
    private void switchToDataAccessView(){
        try {
            DemoApp.setRoot("primary");
        } catch (IOException e) {
            System.out.println("Switching scenes failed.");
            throw new RuntimeException(e);
        }
    }

    private boolean authenticateUser(String email, String password){
        String UID;
        try {
            UID = DemoApp.fauth.getUserByEmail(email).getUid();
        } catch (FirebaseAuthException e) {
            System.out.println("Failed to get User ID");
            return false;
        }

        ApiFuture<DocumentSnapshot> future = DemoApp.fstore.collection("Users").document(UID).get();

        String validPassword = null;

        try {
            DocumentSnapshot snapshot = future.get();
            validPassword = snapshot.getString("password");
        } catch (Exception e) {
            System.out.println("Error getting password");
        }

        return validPassword.equals(password);
    }

    private boolean registerUser() {
        String email = emailField.getText();
        String password = passwordField.getText();

        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setEmailVerified(false)
                .setPassword(password)
                .setPhoneNumber("+11234567890")
                .setDisplayName("John Doe")
                .setDisabled(false);

        UserRecord userRecord;
        try {
            System.out.println("here");
            userRecord = DemoApp.fauth.createUser(request);
            System.out.println("Here");
            CurrentUser = userRecord.getUid();
            System.out.println("Successfully created new user with Firebase Uid: " + CurrentUser
                    + " check Firebase > Authentication > Users tab");
            return true;

        } catch (FirebaseAuthException ex) {
            // Logger.getLogger(FirestoreContext.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error creating a new user in the firebase");
            return false;
        }
    }

    private void addUserToDB() {

        DocumentReference docRef = DemoApp.fstore.collection("Users").document(CurrentUser);

        Map<String, Object> data = new HashMap<>();
        data.put("password", passwordField.getText());

        //asynchronously write data
        ApiFuture<WriteResult> result = docRef.set(data);
    }
}
