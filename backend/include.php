<?php

//ini_set('display_errors', 1);

const API_KEY = '11e3b037-1b70-4eb6-b44a-5d3837041621';
const DB_HOST = 'mariadb';
const DB_USER = 'root';
//const DB_PASS = 'supersecretrootpassword';
const DB_PASS = 'strongrootpassword';
const BASE_URL = 'https://kazbekovandrew.fvds.ru/';
const IMAGE_EXTENSION = 'jpg';

const DB_OPTIONS = [
    PDO::ATTR_EMULATE_PREPARES => false, // Disable emulation mode for "real" prepared statements
    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION, // Disable errors in the form of exceptions
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC, // Make the default fetch be an associative array
];

function check_api_key()
{
    $request_headers = apache_request_headers();
    if ($request_headers['AUTHKEY'] !== API_KEY) {
        send_response(403);
    }
}

function send_response($code = 405, $message = null, $data = null)
{
    if (is_null($message))
        switch ($code) {
            case 200:
                $message = 'OK';
                break;
            case 400:
                $message = 'Bad Request';
                break;
            case 401:
                $message = 'Unauthorized';
                break;
            case 404:
                $message = 'Not found';
                break;
            case 403:
                $message = 'Forbidden';
                break;
            case 405:
                $message = 'Method Not Allowed';
                break;
            case 409:
                $message = 'Conflict';
                break;
            case 410:
                $message = 'Gone';
                break;
            case 423:
                $message = 'Locked';
                break;
            case 500:
                $message = 'Internal Server Error';
                break;
            case 503:
                $message = 'Service Unavailable';
                break;
        }

    header('HTTP/1.1 ' . $code . ' ' . $message);
    header('x-error-message: ' . $message);
    if (!is_null($data)) {
        header('Content-type: application/json');
        echo json_encode($data, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    }
    http_response_code($code);
    die();
}

return new PDO('mysql:host=' . DB_HOST . ';dbname=api;charset=utf8mb4', DB_USER, DB_PASS, DB_OPTIONS);
