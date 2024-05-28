<?php

/** @var PDO $pdo */
$pdo = require_once 'include.php';

check_api_key();

try {
    switch ($_SERVER['REQUEST_METHOD']) {
        case 'POST': // Page 1
            $code = intval($_POST['code']);
            $password = $_POST['password'];

            $result = $pdo->query("SELECT code, password, first_name, last_name, trusted_status_id FROM Employee WHERE code = $code LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($result === false) {
                send_response(404);
            }
            if ($result['password'] !== $password) {
                send_response(401);
            }

            send_response(200, data: [
                'employee_status' => $result['trusted_status_id'],
                'first_name' => $result['first_name'],
                'second_name' => $result['last_name'],
            ]);
            break;
        default:
            send_response();
            break;
    }
} catch (Throwable $e) {
    $log = fopen('log.txt', 'a');
    fwrite($log, date('c') . "\t" . $e->getMessage() . "\n");
    fclose($log);

    if (str_contains($e->getMessage(), "Integrity constraint violation")) {
        send_response(400);
    } else {
        send_response(500, $e->getMessage());
    }
}