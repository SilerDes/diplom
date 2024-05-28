<?php

/** @var PDO $pdo */
$pdo = require_once 'include.php';

check_api_key();

try {
    switch ($_SERVER['REQUEST_METHOD']) {
        case 'GET': // Page 5
            $by = intval($_GET['by']);
            $r = intval($_GET['r']);

            $by_employee = $pdo->query("SELECT code, trusted_status_id FROM Employee WHERE code = $by LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($by_employee === false) {
                send_response(404, "Not Found Self");
            }

            $r_employee = $pdo->query("SELECT code, password, trusted_status_id FROM Employee WHERE code = $r LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($r_employee === false) {
                send_response(410);
            }

            if ($by_employee['trusted_status_id'] != 2 && $by_employee['trusted_status_id'] <= $r_employee['trusted_status_id']) {
                send_response(403, "Forbidden By Status");
            }

            send_response(200, data: [
                'pass' => $r_employee['password'],
            ]);
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