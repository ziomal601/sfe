<?php

/**
 * Created by PhpStorm.
 * User: tomek
 * Date: 21.05.2016
 * Time: 21:23
 */
include './libs/php/password_compatibillity.php';

class LoginModel
{

    private $db_connection = null;
    /**v8
     * @var array Collection of error messages
     */
    public $errors = array();
    /**
     * @var array Collection of success / neutral messages
     */
    public $messages = array();
    /**
     * the function "__construct()" automatically starts whenever an object of this class is created,
     * you know, when you do "$login = new Login();"
     */
    public function __construct()
    {
        // create/read session, absolutely necessary
        session_start();
        $this->db_connection = DatabaseConnection::getInstance()->_db;
        // check the possible login actions:
        // if user tried to log out (happen when user clicks logout button)
      
    }
    /**
     * log in with post data
     */
    public function dologinWithPostData($data)
    {

        // check login form contents
        if (empty($data['user_name'])) {
            $this->errors[] = "Username field was empty.";
        } elseif (empty($data['user_password'])) {
            $this->errors[] = "Password field was empty.";
        } elseif (!empty($data['user_name']) && !empty($data['user_password'])) {
            // create a database connection, using the constants from config/db.php (which we loaded in index.php)
            
           
            // if no connection errors (= working database connection)
            
                // escape the POST stuff
                $user_name = ($data['user_name']);
                // database query, getting all the info of the selected user (allows login via email address in the
                // username field)
                $sql = "SELECT login, email, password, permission
                        FROM users
                        WHERE login = '" . $user_name . "' OR email = '" . $user_name . "';";
            
                        
            
                $result_of_login_check = $this->db_connection->query($sql);
            
                $userExist = false;
                $loginUser = null;
                foreach ($result_of_login_check as $user){
                    $userExist = true;
                    $loginUser = $user;
                }
            
                // if this user exists
                if ($userExist) {
                    // get result row (as an object)
                    
                    // using PHP 5.5's password_verify() function to check if the provided password fits
                    // the hash of that user's password
                    if ($data['user_password'] == $loginUser['password']) {
                        // write user data into PHP SESSION (a file on your server)
                        $_SESSION['user_name'] = $loginUser['login'];
                        $_SESSION['user_email'] = $loginUser['email'];
                        $_SESSION['permission'] = $loginUser['permission'];
                        $_SESSION['user_login_status'] = 1;
                        $this->messages = ['login success'];
                    } else {
                        $this->errors[] = "Wrong password. Try again.";
                    }
                } else {
                    $this->errors[] = "This user does not exist.";
                }
            
        }
    }
    /**
     * perform the logout
     */
    public function doLogout()
    {
        // delete the session of the user
        $_SESSION = array();
        session_destroy();
        // return a little feeedback message
        $this->messages[] = "You have been logged out.";
    }
    /**
     * simply return the current state of the user's login
     * @return boolean user's login status
     */
    public function isUserLoggedIn()
    {
        if (isset($_SESSION['user_login_status']) AND $_SESSION['user_login_status'] == 1) {
            return true;
        }
        // default return
        return false;
    }
}