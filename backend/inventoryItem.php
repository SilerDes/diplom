<?php

/** @var PDO $pdo */
$pdo = require_once 'include.php';

check_api_key();

try {
    switch ($_SERVER['REQUEST_METHOD']) {
        case 'POST': // Page 8
            $by = intval($_GET['by']);
            $request_body = json_decode(file_get_contents('php://input'), true, flags: JSON_THROW_ON_ERROR);

            $by_employee = $pdo->query("SELECT code, trusted_status_id FROM Employee WHERE code = $by LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($by_employee === false) {
                send_response(404, "Not Found Self");
            }

            $session_id = intval($request_body['session_id']);
            $session = $pdo->query("SELECT id, datetime_started, datetime_finished, created_by FROM Session WHERE id = $session_id LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($session === false) {
                send_response(410);
            }
            if (!empty($session['datetime_finished'])) {
                send_response(409);
            }
            if ($by_employee['trusted_status_id'] == 0 || ($by_employee['trusted_status_id'] == 1 && $by_employee['code'] != $session['created_by'])) {
                send_response(403, "Forbidden By Status");
            }

            $to_insert = array();
            foreach ($request_body['employee2items'] as $link) {
                foreach ($link['items'] as $item) {
                    $to_insert[] = [
                        'session_id' => $session_id,
                        'employee_code' => $link['employee'],
                        'position_id' => $item['position_id'],
                    ];
                }
            }
            $insert_statement = $pdo->prepare("INSERT INTO InventoryItem (session_id, employee_code, position_id) VALUES (:session_id, :employee_code, :position_id)");
            $pdo->beginTransaction();
            foreach ($to_insert as $data) {
                $insert_statement->execute($data);
            }
            $result = $pdo->commit();

            send_response(200);
            break;
        case 'DELETE': // Page 10
            $by = intval($_GET['by']);
            $id = intval($_GET['id']);

            $by_employee = $pdo->query("SELECT code, trusted_status_id FROM Employee WHERE code = $by LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($by_employee === false) {
                send_response(404, "Not Found Self");
            }

            $item = $pdo->query("SELECT id, session_id, employee_code, position_id FROM InventoryItem WHERE id = $id LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($item === false) {
                send_response(410);
            }

            $session_id = $item['session_id'];
            $session = $pdo->query("SELECT id, created_by FROM Session WHERE id = $session_id LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($by_employee['trusted_status_id'] == 0 || ($by_employee['trusted_status_id'] == 1 && $by_employee['code'] != $session['created_by'])) {
                send_response(403, "Forbidden By Status");
            }

            $pdo->query("DELETE FROM InventoryItem WHERE id = $id LIMIT 1");

            send_response(200);
            break;
        case 'GET': // Page 11
            $by = intval($_GET['by']);
            $session_id = intval($_GET['session_id']);

            $by_employee = $pdo->query("SELECT code, trusted_status_id FROM Employee WHERE code = $by LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($by_employee === false) {
                send_response(404, "Not Found Self");
            }

            $session = $pdo->query("SELECT id, created_by FROM Session WHERE id = $session_id LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($session === false) {
                send_response(410);
            }

            if ($by != $session['created_by'] && $by_employee['trusted_status_id'] != 2) {
                send_response(403, "Forbidden By Status");
            }

            $items = $pdo->query("SELECT 
InventoryItem.id as id,
employee_code, position_id, count,
code, first_name, last_name,
title_official, title_user, image_link
FROM InventoryItem
JOIN Employee on code = employee_code
JOIN InventoryPosition on InventoryPosition.id = position_id
WHERE session_id = $session_id")->fetchAll(PDO::FETCH_ASSOC);

            $employees = array();
            $positions = array();
            foreach ($items as $item) {
                $employees[$item['employee_code']] = [
                    'code' => $item['employee_code'],
                    'first_name' => $item['first_name'],
                    'last_name' => $item['last_name'],
                ];
                $positions[$item['employee_code']][] = [
                    'id' => $item['id'],
                    'position' => [
                        'id' => $item['position_id'],
                        'title_official' => $item['title_official'],
                        'title_user' => $item['title_user'],
                        'image_link' => !empty($item['image_link']) ? BASE_URL . $item['image_link'] : null,
                    ],
                    'count' => $item['count'],
                ];
            }

            $response = [
                'session_id' => $session_id,
                'employee2items' => array()
            ];
            foreach ($employees as $employee) {
                $item = [
                    'employee' => $employee,
                    'items' => $positions[$employee['code']],
                ];
                $response['employee2items'][] = $item;
            }

            send_response(200, data: $response);
            break;
        case 'PUT': // Page 13
            $id = intval($_GET['id']);
            $count = intval($_GET['count']);

            $item = $pdo->query("SELECT id, session_id, employee_code, position_id FROM InventoryItem WHERE id = $id LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($item === false) {
                send_response(410);
            }

            $pdo->query("UPDATE InventoryItem SET count = $count WHERE id = $id LIMIT 1");

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