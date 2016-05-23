<?php

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of DatabaseConnection
 *
 * @author Tomek
 */
require_once("DatabaseConnection.php");

class DatabaseConnection {

    public $_db;
    static $_instance;
    static $_id;

    private function __construct() {
        $this->_db = new PDO('mysql:host=localhost;dbname=sfe', 'root', '');
        $this->_db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        $this->_db->query('SET NAMES utf8');

    }

    private function __clone() {
        
    }

    public static function getInstance() {
        if (!(self::$_instance instanceof self)) {
            self::$_instance = new self();
        }
        return self::$_instance;
    }

    public function getID() {
        return $this->_db->lastInsertId();
    }
    

    public function query($sql) {
        return $this->_db->query($sql);
    }

}
