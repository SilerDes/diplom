<?php

/** @var PDO $pdo */
$pdo = require_once 'include.php';

check_api_key();

try {
    switch ($_SERVER['REQUEST_METHOD']) {
        case 'GET':
            $by = intval($_GET['by']);
            $required_title = $_GET['requiredTitle'];

            $by_employee = $pdo->query("SELECT code, trusted_status_id FROM Employee WHERE code = $by LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($by_employee === false) {
                send_response(404, "Not Found Self");
            }
            if ($by_employee['trusted_status_id'] < 1) {
                send_response(403, "Forbidden by status");
            }

            $statement = $pdo->prepare("SELECT id FROM InventoryPosition WHERE title_official = :required_title LIMIT 1");
            $statement->execute(['required_title' => $required_title]);
            $result = $statement->fetch(PDO::FETCH_ASSOC);
            if ($result === false) {
                send_response(200);
            }

            send_response(423);
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