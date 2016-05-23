<?php

/**
 * Created by PhpStorm.
 * User: tomek
 * Date: 21.05.2016
 * Time: 22:03
 */
include './libs/php/password_compatibillity.php';

class RegistrationModel
{
    /**
     * @var object $db_connection The database connection
     */
    private $db_connection = null;
    /**
     * @var array $errors Collection of error messages
     */
    public $errors = array();
    /**
     * @var array $messages Collection of success / neutral messages
     */
    public $messages = array();
    /**
     * the function "__construct()" automatically starts whenever an object of this class is created,
     * you know, when you do "$registration = new Registration();"
     */
    private $postData = null;

    public function __construct()
    {

        $this->db_connection = DatabaseConnection::getInstance()->_db;

        var_dump($_POST);
        $this->postData = array();
        parse_str($_POST['data'], $this->postData);
        var_dump($this->postData['user_permission']);
        $this->registerNewUser();
    }

    /**
     * handles the entire registration process. checks all error possibilities
     * and creates a new user in the database if everything is fine
     */
    private function registerNewUser()
    {
        if (empty($this->postData['user_name'])) {
            $this->errors[] = "Empty Username";
        } elseif (empty($this->postData['user_password_new']) || empty($this->postData['user_password_repeat'])) {
            $this->errors[] = "Empty Password";
        } elseif ($this->postData['user_password_new'] !== $this->postData['user_password_repeat']) {
            $this->errors[] = "Password and password repeat are not the same";
        } elseif (strlen($this->postData['user_password_new']) < 6) {
            $this->errors[] = "Password has a minimum length of 6 characters";
        } elseif (strlen($this->postData['user_name']) > 64 || strlen($this->postData['user_name']) < 2) {
            $this->errors[] = "Username cannot be shorter than 2 or longer than 64 characters";
        } elseif (!preg_match('/^[a-z\d]{2,64}$/i', $this->postData['user_name'])) {
            $this->errors[] = "Username does not fit the name scheme: only a-Z and numbers are allowed, 2 to 64 characters";
        } elseif (empty($this->postData['user_email'])) {
            $this->errors[] = "Email cannot be empty";
        } elseif (strlen($this->postData['user_email']) > 64) {
            $this->errors[] = "Email cannot be longer than 64 characters";
        } elseif (!filter_var($this->postData['user_email'], FILTER_VALIDATE_EMAIL)) {
            $this->errors[] = "Your email address is not in a valid email format";
        } elseif (!empty($this->postData['user_name'])
            && strlen($this->postData['user_name']) <= 64
            && strlen($this->postData['user_name']) >= 2
            && preg_match('/^[a-z\d]{2,64}$/i', $this->postData['user_name'])
            && !empty($this->postData['user_email'])
            && strlen($this->postData['user_email']) <= 64
            && filter_var($this->postData['user_email'], FILTER_VALIDATE_EMAIL)
            && !empty($this->postData['user_password_new'])
            && !empty($this->postData['user_password_repeat'])
            && ($this->postData['user_password_new'] === $this->postData['user_password_repeat'])
        ) {

            $user_name = $this->postData['user_name'];
            $user_email = $this->postData['user_email'];
            $user_password = $this->postData['user_password_new'];
            // crypt the user's password with PHP 5.5's password_hash() function, results in a 60 character
            // hash string. the PASSWORD_DEFAULT constant is defined by the PHP 5.5, or if you are using
            // PHP 5.3/5.4, by the password hashing compatibility library
             //$user_password_hash = password_hash($user_password, PASSWORD_DEFAULT);
             $user_password_hash = $user_password;
            // check if user or email address already exists
            $sql = "SELECT * FROM users WHERE login = '" . $user_name . "' OR email = '" . $user_email . "';";
            $query_check_user_name = $this->db_connection->query($sql);

            $userExist = false;
            foreach ($query_check_user_name as $user) {

                $userExist = true;
            }

            if ($userExist) {
                $this->errors[] = "Sorry, that username / email address is already taken.";
            } else {
                // write new user's data into database
                $sql = "INSERT INTO users (login, password, email, permission)
                            VALUES('" . $user_name . "', '" . $user_password_hash . "', '" . $user_email . "', '" . $this->postData['user_permission'] . "');";
                $query_new_user_insert = $this->db_connection->query($sql);
                // if user has been added successfully
                if ($this->db_connection->lastInsertId()) {
                    $this->messages[] = "Your account has been created successfully. You can now log in.";
                } else {
                    $this->errors[] = "Sorry, your registration failed. Please go back and try again.";
                }
            }

        } else {
            $this->errors[] = "An unknown error occurred.";
        }
    }
}