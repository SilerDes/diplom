<?php

/** @var PDO $pdo */
$pdo = require_once 'include.php';

check_api_key();

try {
    switch ($_SERVER['REQUEST_METHOD']) {
        case 'GET': // Page 24
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

            $items = $pdo->query("SELECT 
InventoryItem.id AS id,
employee_code, position_id, count,
code, first_name, last_name,
title_official, title_user, image_link
FROM InventoryItem
JOIN Employee on code = employee_code
JOIN InventoryPosition on InventoryPosition.id = position_id
WHERE employee_code = $by AND session_id = $session_id AND position_id > 0")->fetchAll(PDO::FETCH_ASSOC);

            $positions = array();
            $id = 0;
            foreach ($items as $item) {
                $positions[] = [
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
                'items' => $positions,
            ];

            send_response(200, data: $response);
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