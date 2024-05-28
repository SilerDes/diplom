<?php

/** @var PDO $pdo */
$pdo = require_once 'include.php';

check_api_key();

try {
    switch ($_SERVER['REQUEST_METHOD']) {
        case 'POST': // Page 2
            $employee_code = intval($_POST['employee_code']);
            $first_name = $_POST['first_name'];
            $second_name = $_POST['second_name'];
            $password = $_POST['password'];
            $trusted_status_id = intval($_POST['trusted_status_id']);
            $admin_code = intval($_POST['admin_code']);

            $admin = $pdo->query("SELECT code, trusted_status_id FROM Employee WHERE code = $admin_code LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($admin === false) {
                send_response(404);
            }
            if ($admin['trusted_status_id'] < 1) {
                send_response(403, 'Forbidden By Status');
            }
            if ($admin['trusted_status_id'] != 2 && $trusted_status_id >= $admin['trusted_status_id']) {
                send_response(403, 'Forbidden By Status');
            }
            $new = $pdo->query("SELECT code FROM Employee WHERE code = $employee_code LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($new !== false) {
                send_response(409);
            }
            $statement = $pdo->prepare("INSERT INTO Employee (code, first_name, last_name, password, trusted_status_id) VALUE (:code, :first_name, :second_name, :password, :trusted_status_id)");
            $statement->execute([
                'code' => $employee_code,
                'first_name' => $first_name,
                'second_name' => $second_name,
                'password' => $password,
                'trusted_status_id' => $trusted_status_id
            ]);

            send_response(200);
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