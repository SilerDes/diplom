<?php

/** @var PDO $pdo */
$pdo = require_once 'include.php';

check_api_key();

try {
    switch ($_SERVER['REQUEST_METHOD']) {
        case 'POST': // Page 20
            $by = intval($_GET['by']);
            $session_id = intval($_GET['session_id']); // Page 23

            $by_employee = $pdo->query("SELECT code, trusted_status_id FROM Employee WHERE code = $by LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($by_employee === false) {
                send_response(404, "Not Found Self");
            }

            if ($session_id == 0) {
                if ($by_employee['trusted_status_id'] < 1) {
                    send_response(403, "Forbidden By Status");
                }

                $pdo->query("INSERT INTO Session (datetime_started, created_by) VALUE (CURRENT_TIMESTAMP(), $by) LIMIT 1");
                $session = $pdo->query("SELECT * FROM Session WHERE id = " . $pdo->lastInsertId())->fetch(PDO::FETCH_ASSOC);

                send_response(200, data: $session);
            } else {
                $session = $pdo->query("SELECT * FROM Session WHERE id = $session_id")->fetch(PDO::FETCH_ASSOC);
                if ($session === false) {
                    send_response(410);
                }
                if ($by_employee['trusted_status_id'] == 0 || ($by_employee['trusted_status_id'] == 1 && $session['created_by'] != $by_employee['code'])) {
                    send_response(403, "Forbidden By Status");
                }

                $pdo->query("UPDATE Session SET datetime_finished = CURRENT_TIMESTAMP() WHERE id = $session_id")->execute();
                send_response(200, data: ['datetime_finished' => time()]);
            }
            break;
        case 'DELETE': // Page 21
            $by = intval($_GET['by']);
            $session_id = intval($_GET['session_id']);

            $by_employee = $pdo->query("SELECT code, trusted_status_id FROM Employee WHERE code = $by LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($by_employee === false) {
                send_response(404, "Not Found Self");
            }

            $session = $pdo->query("SELECT * FROM Session WHERE id = $session_id")->fetch(PDO::FETCH_ASSOC);
            if ($session === false) {
                send_response(410);
            }

            if ($by_employee['trusted_status_id'] == 0 || ($by_employee['trusted_status_id'] == 1 && $session['created_by'] != $by_employee['code'])) {
                send_response(403, "Forbidden By Status");
            }

            $pdo->beginTransaction();
            $pdo->query("DELETE FROM InventoryItem WHERE session_id = $session_id")->execute();
            $pdo->query("DELETE FROM Session WHERE id = $session_id")->execute();
            $pdo->commit();

            send_response(200);
            break;
        case 'GET': // Page 22
            $by = intval($_GET['by']);

            $by_employee = $pdo->query("SELECT code, trusted_status_id FROM Employee WHERE code = $by LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($by_employee === false) {
                send_response(404, "Not Found Self");
            }

            if ($by_employee['trusted_status_id'] == 0) {
                send_response(403, "Forbidden By Status");
            }
            $where = '';
            if ($by_employee['trusted_status_id'] == 1) {
                $where = "WHERE created_by = $by";
            }
            $items = $pdo->query("SELECT s.id, s.datetime_started, s.datetime_finished,
     e.code, e.first_name, e.last_name, e.trusted_status_id,
     t.title as trusted_status_title FROM Session s
     JOIN Employee e ON s.created_by = e.code
     JOIN TrustedStatus t ON e.trusted_status_id = t.id 
     $where")->fetchAll(PDO::FETCH_ASSOC);
            $sessions = array();
            foreach ($items as $item) {
                $sessions[] = [
                    'id' => $item['id'],
                    'datetime_started' => $item['datetime_started'],
                    'datetime_finished' => $item['datetime_finished'],
                    'created_by' => [
                        'code' => $item['code'],
                        'first_name' => $item['first_name'],
                        'last_name' => $item['last_name'],
                        'trusted_status_id' => $item['trusted_status_id'],
                        'trusted_status_title' => $item['trusted_status_title']
                    ],
                ];
            }

            send_response(200, data: ['sessions' => $sessions]);
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