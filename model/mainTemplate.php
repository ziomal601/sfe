<?php

/**
 * Created by PhpStorm.
 * User: tomek
 * Date: 22.05.2016
 * Time: 14:10
 */
class MainTemplate 
{
    static $FIELDS = array(
        'images_main' =>array('url','name'),
        'welcome_text' =>array('text'),
        'content_main'=>array('header','font','content')
    );
    static $EDITABLE_FIELDS = array('images_main','content_main','welcome_text');
    private $database = null;
    function __construct($id)
    {
        $this->database = DatabaseConnection::getInstance();
        $this->fillPage($id);

    }
    function fillPage($id){
        $menu = $this->getMenu();
        $images = array();
        /*
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

        );*/

        $counter = $this->database->query("SELECT DISTINCT counter  FROM content WHERE resource like '%images_main%' and page_id=".$id);

        foreach ($counter as $count) {
            $rows = $this->database->query("SELECT * FROM content WHERE resource like '%images_main%' and page_id=" . $id . " and counter=" . $count['counter']);
            $tempArray = array();
            foreach ($rows as $row) {
                if (strpos($row['resource'], 'url') !== false) {
                    $tempArray['url']= $row['content'];
                }
                if (strpos($row['resource'], 'name') !== false) {
                    $tempArray['name'] = $row['content'];
                }

            }
            array_push($images,$tempArray);
        }


        $welcomeText = 'We create the best software on the World';

        $counter = $this->database->query("SELECT DISTINCT counter  FROM content WHERE resource like '%welcome_text%' and page_id=".$id);

        foreach ($counter as $count) {
            $rows = $this->database->query("SELECT * FROM content WHERE resource like '%welcome_text%' and page_id=" . $id . " and counter=" . $count['counter']);

            foreach ($rows as $row) {
                $welcomeText = $row['content'];
            }

        }
        $content = array();
        /*
        $content = array(
            array(
                'header' => 'Programowanie to nasza pasjaaaaa',
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
        );*/

        $counter = $this->database->query("SELECT DISTINCT counter  FROM content WHERE resource like '%content_main%' and page_id=".$id);

        foreach ($counter as $count) {
            $rows = $this->database->query("SELECT * FROM content WHERE resource like '%content_main%' and page_id=" . $id . " and counter=" . $count['counter']);
            $tempArray = array();
            foreach ($rows as $row) {
                if (strpos($row['resource'], 'header') !== false) {
                    $tempArray['header']= $row['content'];
                }
                if (strpos($row['resource'], 'font') !== false) {
                    $tempArray['font'] = $row['content'];
                }if (strpos($row['resource'], 'content') !== false) {
                    $tempArray['content'] = $row['content'];
                }

            }
            array_push($content,$tempArray);
        }
        include './view/templates/mainTemplates.phtml';
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