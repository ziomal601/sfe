<?php
/**
 * Created by PhpStorm.
 * User: tomek
 * Date: 14.05.2016
 * Time: 21:22
 */

require_once './model/PageModel.php';
require_once './model/AdminModel.php';




class PageController
{
    function __construct($getParams,$postParams){

        $parts = $this->getPath();
       // var_dump($parts);
        if(isset($parts[1])) {
            if ($parts[1] == 'admin') {
                if(isset($parts[2])){
                    $adminModel = new AdminModel();
                    $adminModel->renderContentObject($parts[2],$_POST);
                }else{
                    $adminModel = new AdminModel();
                    $adminModel->renderHomePage($_POST);
                }



            } else if ($parts[1] == 'page' || $parts[1] == 'index') {
               if(isset($parts[2])){
                new PageModel($parts[2]);
               }else{
                   new pageModel(null);
               }

            }
        }else{

        }

    }
   
    function getPath()
    {
        return array_values(array_filter(explode('/', $_SERVER['REQUEST_URI'])));
    }
    function sec_session_start() {
        $session_name = 'sec_session_id';   // Set a custom session name
        $secure = true;
        // This stops JavaScript being able to access the session id.
        $httponly = true;
        // Forces sessions to only use cookies.
        if (ini_set('session.use_only_cookies', 1) === FALSE) {
            header("Location: ../error.php?err=Could not initiate a safe session (ini_set)");
            exit();
        }
        // Gets current cookies params.
        $cookieParams = session_get_cookie_params();
        session_set_cookie_params($cookieParams["lifetime"],
            $cookieParams["path"],
            $cookieParams["domain"],
            $secure,
            $httponly);
        // Sets the session name to the one set above.
        session_name($session_name);
        session_start();            // Start the PHP session
        session_regenerate_id(true);    // regenerated the session, delete the old one.
    }
}



