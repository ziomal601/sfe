<?php

/**
 * Created by PhpStorm.
 * User: tomek
 * Date: 22.05.2016
 * Time: 15:35
 */
class ProjectTemplate
{
    static $FIELDS = array(
        'welcome_text' =>array('text'),
        'main_content_proj'=>array('header','subHeader','image','content')
    );
    static $EDITABLE_FIELDS = array('main_content_proj','welcome_text');
    private $database = null;
    function __construct($id)
    {
        $this->database = DatabaseConnection::getInstance();
        $this->fillPage($id);

    }
    function fillPage($id){

        $menu = $this->getMenu();
        $welcomeText = 'We create the best software on the World';
        $counter = $this->database->query("SELECT DISTINCT counter  FROM content WHERE resource like '%welcome_text%' and page_id=".$id);

        foreach ($counter as $count) {
            $rows = $this->database->query("SELECT * FROM content WHERE resource like '%welcome_text%' and page_id=" . $id . " and counter=" . $count['counter']);

            foreach ($rows as $row) {
                $welcomeText = $row['content'];
            }

        }
        $mainContent = array();
        /*
        $mainContent = array(
            array(
                'header' => 'Tomasz Malski',
                'subHeader' => 'CEO',
                'image' => 'http://geekfestival.pl/assets/images/speakers/Malski%20Tomasz.jpg',
                'content' => 'CEO, gówno wie, gówno robi, ale przynajmniej ogarnia ten cały syf'
            ),array(
                'header' => 'Adrian Ziemecki',
                'subHeader' => 'Robol',
                'image' => 'https://avatars0.githubusercontent.com/u/11388337?v=3&s=400',
                'content' => 'Odawala całą robote, robi zajebistą dokumentacje ale i tak CEO dostaje pochwały'
            ),

        );
        */
        $counter = $this->database->query("SELECT DISTINCT counter  FROM content WHERE resource like '%main_content_proj%' and page_id=".$id);

        foreach ($counter as $count) {
            $rows = $this->database->query("SELECT * FROM content WHERE resource like '%main_content_proj%' and page_id=" . $id . " and counter=" . $count['counter']);
            $tempArray = array();
            foreach ($rows as $row) {
                if (strpos($row['resource'], 'header') !== false) {
                    $tempArray['header']= $row['content'];
                }
                if (strpos($row['resource'], 'subHeader') !== false) {
                    $tempArray['subHeader'] = $row['content'];
                }
                if (strpos($row['resource'], 'image') !== false) {
                    $tempArray['image'] = $row['content'];
                }
                if (strpos($row['resource'], 'content') !== false) {
                    $tempArray['content'] = $row['content'];
                }

            }
            array_push($mainContent,$tempArray);
        }

        include './view/templates/projectTemplate.phtml';
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