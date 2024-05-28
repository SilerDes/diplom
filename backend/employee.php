<?php

/** @var PDO $pdo */
$pdo = require_once 'include.php';

check_api_key();

try {
    switch ($_SERVER['REQUEST_METHOD']) {
        case 'DELETE': // Page 3
            $by = intval($_GET['by']);
            $d = intval($_GET['d']);

            $by_employee = $pdo->query("SELECT code, trusted_status_id FROM Employee WHERE code = $by LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($by_employee === false) {
                send_response(404, "Not Found Self");
            }

            $d_employee = $pdo->query("SELECT code, trusted_status_id FROM Employee WHERE code = $d LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($d_employee === false) {
                send_response(410);
            }

            if ($by_employee['trusted_status_id'] == $d_employee['trusted_status_id']) {
                send_response(423);
            }
            if ($by_employee['trusted_status_id'] != 2 && $by_employee['trusted_status_id'] <= $d_employee['trusted_status_id']) {
                send_response(403, "Forbidden By Status");
            }

            $pdo->beginTransaction();
            $pdo->query("UPDATE Session SET created_by = 0 WHERE created_by = $d")
                ->execute();
            $pdo->query("UPDATE InventoryItem SET employee_code = 0 WHERE employee_code = $d");
            $pdo->query("DELETE FROM Employee WHERE code = $d");
            $result = $pdo->commit();

            send_response(200);
            break;
        case 'GET': // Page 4
            $by = intval($_GET['by']);
            $s = intval($_GET['s']); // Page 6

            $by_employee = $pdo->query("SELECT code, trusted_status_id FROM Employee WHERE code = $by LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($by_employee === false) {
                send_response(404);
            }
            if ($by_employee['trusted_status_id'] < 1) {
                send_response(403, "Forbidden By Status");
            }

            $query = "SELECT code, first_name, last_name, trusted_status_id, title as trusted_status_title FROM Employee LEFT JOIN TrustedStatus on trusted_status_id = id WHERE ";
            if ($s > 0) {
                $query .= "code = $s";
            } else {
                $query .= "code > 0";
            }
            $rows = $pdo->query($query)->fetchAll(PDO::FETCH_ASSOC);

            send_response(200, data: [
                'employees' => $rows
            ]);
            break;
        case 'PUT': // Page 7
            $input = file_get_contents('php://input');
            parse_str(urldecode($input), $_POST);
            $by = intval($_POST['by']);
            $updatable = intval($_POST['updatable']);
            $first_name = $_POST['first_name'];
            $second_name = $_POST['second_name'];
            $trusted_status_id = intval($_POST['trusted_status_id']);
            $password = $_POST['password']; // nullable

            $by_employee = $pdo->query("SELECT code, trusted_status_id FROM Employee WHERE code = $by LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($by_employee === false) {
                send_response(404, "Not Found Self");
            }

            $updatable_employee = $pdo->query("SELECT code, trusted_status_id FROM Employee WHERE code = $updatable LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($updatable_employee === false) {
                send_response(404);
            }

            if (!($by == $updatable) && $by_employee['trusted_status_id'] != 2 && $by_employee['trusted_status_id'] <= $updatable_employee['trusted_status_id']) {
                send_response(403, "Forbidden By Status");
            }

            $update_params = [
                'first_name' => $first_name,
                'last_name' => $second_name,
            ];
            if (!empty($password)) {
                $update_params['password'] = $password;
            }
            if ($by_employee['trusted_status_id'] == 2 && $by != $updatable) {
                $update_params['trusted_status_id'] = $trusted_status_id;
            }

            $update_part = array();
            foreach ($update_params as $key => $value) {
                $update_part[] = $key . ' = :' . $key;
            }

            $query = "UPDATE Employee SET " . implode(', ', $update_part) . " WHERE code = $updatable LIMIT 1";
            $pdo->prepare($query)->execute($update_params);

            send_response(200);
            break;
        default:
            send_response();
    }
} catch (Throwable $e) {
    if ($pdo->inTransaction()) {
        $pdo->rollback();
    }

    $log = fopen('log.txt', 'a');
    fwrite($log, date('c') . "\t" . $e->getMessage() . "\n");
    fclose($log);

    if (str_contains($e->getMessage(), "Integrity constraint violation")) {
        send_response(400);
    } else {
        send_response(500, $e->getMessage());
    }
}