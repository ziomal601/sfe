<?php

/**
 * Created by PhpStorm.
 * User: tomek
 * Date: 14.05.2016
 * Time: 21:45
 */
require_once 'DatabaseConnection.php';
include 'MainTemplate.php';
include 'AboutTemplate.php';
include 'ProjectTemplate.php';

class PageModel
{
    function __construct($name){

        $menu = $this->getMenu();
        
        if(isset($name)){
            $this->getSpecificPage($name);
        }
        else {


           
            $images = array(
                array(
                    'url' => "https://www.cultivate-communications.com/wp-content/uploads/2015/02/Rosy_Coder.jpg",
                    'name' => "We code anythink"
                ),
                array(
                    'url' => "http://edu-sourcing.com/wp-content/uploads/2015/11/GirlProgrammer.jpg",
                    'name' => "Girls work with us :)"
                ),
                array(
                    'url' => "http://www.newenglandcollegeonline.com/media/3844116/computer-programmer.jpg",
                    'name' => "We are awersome"
                )

            );
            $welcomeText = 'We create the best software on the World';
            $content = array(
                array(
                    'header' => 'Programowanie to nasza pasja',
                    'font' => 'fa-check',
                    'content' => 'Nasz CEO chodzi i rozkazuje mówić, że programowanie to nasz chleb powszedni'
                ),
                array(
                    'header' => 'Zawsze zostajemt po godzinach',
                    'font' => 'fa-gift',
                    'content' => 'Zakres naszych projektów jest tak ogromny, a że każdy nas kocha programowanie - zostajemy za darmo po godzinach'
                ),
                array(
                    'header' => 'Nie wiem co',
                    'font' => 'fa-compass',
                    'content' => 'Zrobimy i tyle'
                )
            );
            include './view/templates/mainTemplates.phtml';
        }

    }
   
    function getInformationAboutPage(){
        
        
        
    }
    function getMainPage(){
        
    }
    function fillPage($id){

    }
    
    final private function getSpecificPage($name){
        $database = DatabaseConnection::getInstance();
        $rows = $database->query("SELECT * FROM pages WHERE name ='".$name."'");
        $page = null;
        foreach ($rows as $row){
            $page = new $row['template_name']($row['page_id']);
        }
        if($page != null){
                
        }else{
            //TODO errorPage
        }
        

    }
    function getMenu()
    {

        $database = DatabaseConnection::getInstance();
        $rows = $database->query('SELECT name FROM pages');

        $menu = array();
        foreach ($rows as $row) {

            $menu[] = $row['name'];
        }

        return $menu;

    }
}