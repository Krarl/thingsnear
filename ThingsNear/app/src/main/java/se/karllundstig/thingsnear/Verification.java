package se.karllundstig.thingsnear;

public abstract class Verification {

    static public boolean isUsernameValid(String username) {
        //tillåt inte flera ord eller mellanslag
        return !username.contains(" ");
    }

    static public boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    static public boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }
}
