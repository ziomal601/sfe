<?php

/**
 * Created by PhpStorm.
 * User: tomek
 * Date: 18.05.2016
 * Time: 21:52
 */
include 'RegistrationModel.php';
include  'LoginModel.php';

class AdminModel
{


    function __construct()
    {

    }

    public function renderHomePage($data)
    {

        $login = new LoginModel();
        if (session_status() == PHP_SESSION_NONE) {
            Echo 'session is ok'.$_SESSION['user_login_status'];
        }
       if($login->isUserLoggedIn()){

           include '/view/admin/adminPageTemplate.phtml';
        }else{
           $message = null;
           $error = null;
           if(isset($_POST['user_name'])) {
               $login->dologinWithPostData($data);
               $message =   $login->messages;
               $error = $login->errors;
               if(count($error)==0){
                   include '/view/admin/adminPageTemplate.phtml';
                   return '';
               }else{
                   include '/view/admin/loginTemplate.phtml';
                   return '';
               }
           }
           include '/view/admin/loginTemplate.phtml';
        }
        
       
    }

    public function renderContentObject($page, $data)
    {
        $login = new LoginModel();
        if ($page == "logout"){
            $login->doLogout();
            $message =   $login->messages;
            $error = $login->errors;
            include '/view/admin/loginTemplate.phtml';
            return'';
        }
        if ($page == 'post') {
            switch ($data['page']) {
                case 'main': {
                    $this->renderMainSettings();
                    break;
                }
                case 'management': {
                    $this->renderManagementsettings($data);
                    break;
                }
                case 'users': {
                    $this->renderUsersSettings($data);
                    break;
                }
                case 'other': {
                    echo 'other';
                    break;
                }
            }
        } elseif ($page == 'edit') {
            var_dump($data);
        }
    }

    function renderMainSettings()
    {
        include('./view/admin/mainTemplate.phtml');
    }
    function renderUsersSettings($data){

        $message = null;
        $error = null;
        if (isset($data['action'])) {
            if ($data['action'] == 'add') {
               $register = new RegistrationModel();
               $error =  $register->errors;
               $message = $register->messages; 
            }
            if ($data['action'] == 'delete') {


            }
            
        }
        
        
           include('./view/admin/userTemplate.phtml');
    }
    function renderManagementsettings($data)
    {

        $database = DatabaseConnection::getInstance();

        $edit = false;
        $mainContent = array();
        $fields = null;
        $editPageId = null;
        $editable = null;
        $message = null;
        if (isset($data['action'])) {
            if ($data['action'] == 'show') {
                $edit = true;
                $editPageId = $data['id'];
                $pageTemplates = $database->query('SELECT template_name FROM pages WHERE page_id=' . $data['id']);
                foreach ($pageTemplates as $template) {
                    if ($template['template_name'] == 'AboutTemplate') {
                        $fields = AboutTemplate::$FIELDS;
                        foreach (AboutTemplate::$EDITABLE_FIELDS as $field){
                            $editable[$field] = $fields[$field];
                        }
                        foreach ($fields as $key => $value){
                            $counter = $database->query("SELECT DISTINCT counter  FROM content WHERE resource like '%".$key."%' and page_id=".$data['id']);

                            foreach ($counter as $count) {
                                $rows = $database->query("SELECT * FROM content WHERE resource like '%".$key."%' and page_id=" . $data['id'] . " and counter=" . $count['counter']);
                                $tempArray = array();
                                foreach ($rows as $row) {

                                    foreach ($value as $item){
                                        if (strpos($row['resource'], $item) !== false) {
                                            $tempArray[$key.'_'.$item]= $row['content'];
                                        }
                                    }
                                }
                                $mainContent[$key.'-'.$count['counter']]=$tempArray;
                            }

                        }
                    }
                    if ($template['template_name'] == 'ProjectTemplate') {

                        $fields = ProjectTemplate::$FIELDS;
                        foreach (ProjectTemplate::$EDITABLE_FIELDS as $field){
                            $editable[$field] = $fields[$field];
                        }
                        foreach ($fields as $key => $value){
                            $counter = $database->query("SELECT DISTINCT counter  FROM content WHERE resource like '%".$key."%' and page_id=".$data['id']);

                            foreach ($counter as $count) {
                                $rows = $database->query("SELECT * FROM content WHERE resource like '%".$key."%' and page_id=" . $data['id'] . " and counter=" . $count['counter']);
                                $tempArray = array();
                                foreach ($rows as $row) {

                                    foreach ($value as $item){
                                        if (strpos($row['resource'], $item) !== false) {
                                            $tempArray[$key.'_'.$item]= $row['content'];
                                        }
                                    }
                                }
                                $mainContent[$key.'-'.$count['counter']]=$tempArray;
                            }

                        }
                    }
                    if ($template['template_name'] == 'MainTemplate') {

                        $fields = MainTemplate::$FIELDS;

                        foreach (MainTemplate::$EDITABLE_FIELDS as $field){
                            $editable[$field] = $fields[$field];
                        }

                        foreach ($fields as $key => $value){
                            $counter = $database->query("SELECT DISTINCT counter  FROM content WHERE resource like '%".$key."%' and page_id=".$data['id']);

                            foreach ($counter as $count) {
                                $rows = $database->query("SELECT * FROM content WHERE resource like '%".$key."%' and page_id=" . $data['id'] . " and counter=" . $count['counter']);
                                $tempArray = array();
                                foreach ($rows as $row) {

                                    foreach ($value as $item){
                                        if (strpos($row['resource'], $item) !== false) {
                                            $tempArray[$key.'_'.$item]= $row['content'];
                                        }
                                    }
                                }
                                $mainContent[$key.'-'.$count['counter']]=$tempArray;
                            }
                        }
                    }
                }

            } else if ($data['action'] == 'edit') {
                    $params = array();
                parse_str($data['data'], $params);

                foreach ($params as $key=>$value){
                    $parts = explode('__',$key);
                    $item = explode('-',$parts[0]);
                    $param = $parts[1];
                    $counter = $item[1];
                    $database->query("UPDATE content 
                                        SET `content` = '".$value."' 
                                        WHERE `page_id` = ".$data['id']."
                                        AND   `counter` = ".$counter."
                                        AND   `resource` ='".$param."'
                                        ");
                    $message= 'PAGE WAS UPDATED';
                }
            }else if ($data['action'] == 'addition') {
                $params = array();
                parse_str($data['data'], $params);
                if(count($params) >0 ){
                    $value = reset($params);
                    foreach ($params as $key1=>$value1){
                        $value = $key1;
                    }

                    $counter = $database->query("SELECT DISTINCT max(counter) ma  FROM content WHERE resource like '%".$value."%' and page_id=".$data['id']);
                        $countElem = 1;
                    echo "SELECT DISTINCT max(counter) ma  FROM content WHERE resource like '%".$value."%' and page_id=".$data['id']."<br>";
                    foreach ($counter as $counts){
                        $countElem = $counts['ma']+1;
                    }
                    foreach ($params as $key=>$value){
                    $database->query("INSERT into content (`resource`, `counter`, `content`, `page_id`) VALUES
                      ('".$key."',
                       ".$countElem.",
                       '".$value."',
                       ".$data['id']."
                      )");
                    }
                }
                $message= 'PAGE CONTENT WAS ADDED';
            }else if ($data['action'] == 'new') {
                $params = array();
                parse_str($data['data'], $params);
                var_dump($params);
                $database->query("INSERT INTO pages (`name`,`template_name`) VALUES
                ('".$params['name']."','".$params['template']."')
                ");
                $message= 'PAGE '.$params['name'].' WAS ADDED';
            }else if ($data['action'] == 'delete') {
                $params = array();
                parse_str($data['data'], $params);


                    $parts = explode('__',$data['data']);
                    $item = explode('-',$parts[0]);
                    $param = $item[0];
                    $counter = $item[1];
                    $id = $parts[1];
                    $database->query("Delete from content 
                                        WHERE `page_id` = ".$id."
                                        AND   `counter` = ".$counter."
                                        AND   `resource` like '%".$param."%'
                                        ");
                $message= 'PAGE CONTENT WAS DELETED';
            }else if ($data['action'] == 'deletePage') {

                    $database->query("Delete from content 
                                        WHERE `page_id` = ".$data['data']."
                                        ");
                    $database->query("Delete from pages 
                                        WHERE `page_id` = ".$data['data']."
                                        ");
                $message= 'PAGE  WAS DELETED';
            }
        }
        $rows = $database->query("SELECT * from pages");
        $pages = array();
        foreach ($rows as $row) {
            $pages[] = $row;
        }

        include('./view/admin/managementTemplate.phtml');
    }
}