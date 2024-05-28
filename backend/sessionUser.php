<?php

/** @var PDO $pdo */
$pdo = require_once 'include.php';

check_api_key();

try {
    switch ($_SERVER['REQUEST_METHOD']) {
        case 'GET': // Page 25
            $by = intval($_GET['by']);

            $by_employee = $pdo->query("SELECT code, trusted_status_id FROM Employee WHERE code = $by LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($by_employee === false) {
                send_response(404, "Not Found Self");
            }

            $session_ids = $pdo->query("SELECT s.id FROM InventoryItem ii
    JOIN Session s ON s.id = ii.session_id WHERE ii.employee_code = $by AND s.datetime_finished IS NULL
    GROUP BY s.id")
                ->fetchAll(PDO::FETCH_COLUMN, 0);

            $sessions = array();
            if (!empty($session_ids)) {
                $items = $pdo->query("SELECT s.id, s.datetime_started, s.datetime_finished,
    e.code, e.first_name, e.last_name, e.trusted_status_id,
    t.title as trusted_status_title FROM Session s
    JOIN Employee e ON s.created_by = e.code
    JOIN TrustedStatus t ON e.trusted_status_id = t.id 
    WHERE s.id in (" . implode(',', $session_ids) . ")")->fetchAll(PDO::FETCH_ASSOC);
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
            }

            send_response(200, data: ['sessions' => $sessions]);

            send_response(200, data: [
                'sessions' => [
                    'id' => 1,
                    'datetime_started' => time(),
                    'datetime_finished' => null,
                    'created_by' => [
                        'code' => 9999,
                        'first_name' => 'Ivan',
                        'last_name' => 'Ivanov',
                        'trusted_status_id' => 2,
                        'trusted_status_title' => 'Администратор'
                    ]
                ]
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