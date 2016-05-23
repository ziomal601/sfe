<?php

/**
 * Created by PhpStorm.
 * User: tomek
 * Date: 22.05.2016
 * Time: 14:29
 */
class AboutTemplate
{
    static $FIELDS = array(
        'main_content' =>array('url','name','title'),
        'welcome_text' =>array('text'),
        'second_items'=>array('header','job','image','content')
        );
    static $EDITABLE_FIELDS = array('second_items','main_content','welcome_text');

    private $database = null;
    function __construct($id)
    {
        $this->database = DatabaseConnection::getInstance();
        $this->fillPage($id);

    }
    function fillPage($id){




        $menu = $this->getMenu();
        $mainContent = array();
        /*$mainContent = array(
            'url' => "http://hawaiichaufamilyreunion.weebly.com/uploads/3/8/9/3/38939473/7570799_orig.png?217",
            'name' => "Jesteśmy the best w tym co robimt tyle",
            'title' => "Nasz zespóładasd "

        );*/


        $counter = $this->database->query("SELECT DISTINCT counter  FROM content WHERE resource like '%main_content%' and page_id=".$id);

        foreach ($counter as $count) {
            $rows = $this->database->query("SELECT * FROM content WHERE resource like '%main_content%' and page_id=" . $id . " and counter=" . $count['counter']);
            foreach ($rows as $row) {
                if (strpos($row['resource'], 'url') !== false) {
                    $mainContent['url']= $row['content'];
                }
                if (strpos($row['resource'], 'name') !== false) {
                    $mainContent['name'] = $row['content'];
                }
                if (strpos($row['resource'], 'title') !== false) {
                    $mainContent['title'] = $row['content'];
                }
            }
        }


        $welcomeText = 'We create the best software on the World';

        $counter = $this->database->query("SELECT DISTINCT counter  FROM content WHERE resource like '%welcome_text%' and page_id=".$id);

        foreach ($counter as $count) {
            $rows = $this->database->query("SELECT * FROM content WHERE resource like '%welcome_text%' and page_id=" . $id . " and counter=" . $count['counter']);

            foreach ($rows as $row) {
                $welcomeText = $row['content'];
            }

        }


        $secondItems = array();
        /*
        $secondItems = array(
            array(
                'header' => 'Tomasz Malski',
                'job' => 'CEO',
                'image' => 'http://geekfestival.pl/assets/images/speakers/Malski%20Tomasz.jpg',
                'content' => 'CEO, gówno wie, gówno robi, ale przynajmniej ogarnia ten cały syf'
            ),array(
                'header' => 'Adrian Ziemecki',
                'job' => 'Robol',
                'image' => 'https://avatars0.githubusercontent.com/u/11388337?v=3&s=400',
                'content' => 'Odawala całą robote, robi zajebistą dokumentacje ale i tak CEO dostaje pochwały'
            ),

        );*/

        $counter = $this->database->query("SELECT DISTINCT counter  FROM content WHERE resource like '%second_items%' and page_id=".$id);

        foreach ($counter as $count) {
            $rows = $this->database->query("SELECT * FROM content WHERE resource like '%second_items%' and page_id=" . $id . " and counter=" . $count['counter']);
            $tempArray = array();
            foreach ($rows as $row) {
                if (strpos($row['resource'], 'header') !== false) {
                    $tempArray['header']= $row['content'];
                }
                if (strpos($row['resource'], 'job') !== false) {
                    $tempArray['job'] = $row['content'];
                }
                if (strpos($row['resource'], 'image') !== false) {
                    $tempArray['image'] = $row['content'];
                }
                if (strpos($row['resource'], 'content') !== false) {
                    $tempArray['content'] = $row['content'];
                }
            }
            array_push($secondItems,$tempArray);
        }



        include './view/templates/aboutTemplate.phtml';
    }

    function getMenu()
    {


        $rows = $this->database->query('SELECT name FROM pages');

        $menu = array();
        foreach ($rows as $row) {

            $menu[] = $row['name'];
        }

        return $menu;

    }
}