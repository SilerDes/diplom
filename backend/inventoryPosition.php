<?php

/** @var PDO $pdo */
$pdo = require_once 'include.php';

check_api_key();

try {
    switch ($_SERVER["REQUEST_METHOD"]) {
        case 'DELETE': // Page 15
            $by = intval($_GET["by"]);
            $d = intval($_GET["d"]);

            $by_employee = $pdo->query("SELECT code, trusted_status_id FROM Employee WHERE code = $by LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($by_employee === false) {
                send_response(404, "Not Found Self");
            }
            if ($by_employee['trusted_status_id'] < 1) {
                send_response(403, "Forbidden By Status");
            }

            $d_item = $pdo->query("SELECT * FROM InventoryPosition WHERE id = $d LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($d_item === false) {
                send_response(410);
            }

            $image = dirname(__FILE__) . '/' . $d_item['image_link'];
            if (file_exists($image)) {
                unlink($image);
            }
            $pdo->query("UPDATE InventoryItem SET position_id = 0 WHERE position_id = $d")->execute();
            $pdo->query("DELETE FROM InventoryPosition WHERE id = $d LIMIT 1")->execute();

            send_response(200);
            break;
        case 'GET': // Page 16
            $s = addslashes($_GET["s"]); // Page 17

            $query = "SELECT * FROM InventoryPosition WHERE id > 0 ";
            if (!empty($s)) {
                $query .= " AND ( title_official LIKE '%$s%' OR title_user LIKE '%$s%' )";
            }

            $items = $pdo->query($query)->fetchAll(PDO::FETCH_ASSOC);
            foreach ($items as &$item) {
                if (!empty($item['image_link'])) {
                    $item['image_link'] = BASE_URL . $item['image_link'];
                }
            }

            send_response(200, data: ['positions' => $items]);
            break;
        case 'PUT': // Page 18
            $input = file_get_contents('php://input');
            parse_str(urldecode($input), $_POST);
            $by = $_POST["by"];
            $updatable = $_POST["updatable"];
            $title_official = $_POST["title_official"];
            $title_user = $_POST["title_user"];
            $encoded_image = $_POST["encoded_image"];

            $by_employee = $pdo->query("SELECT code, trusted_status_id FROM Employee WHERE code = $by LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($by_employee === false) {
                send_response(404, "Not Found Self");
            }
            if ($by_employee['trusted_status_id'] < 1) {
                send_response(403, "Forbidden By Status");
            }
            $item = $pdo->query("SELECT * FROM InventoryPosition WHERE id = $updatable LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($item === false) {
                send_response(404);
            }

            $to_update = [
                'id' => $updatable,
                'title_official' => $title_official,
                'title_user' => $title_user,
                'image_link' => $item["image_link"],
            ];

            $update_mod = '';
            if (!empty($encoded_image)) {
                if (!empty($item['image_link'])) {
                    $image = dirname(__FILE__) . '/' . $item['image_link'];
                    if (file_exists($image)) {
                        unlink($image);
                    }
                }
                $image_link = 'Images/IMAGE_' . time() . '.' . IMAGE_EXTENSION;
                $image = dirname(__FILE__) . '/' . $image_link;
                file_put_contents($image, base64_decode($encoded_image));
                $to_update['image_link'] = $image_link;
            }

            $pdo->prepare("UPDATE InventoryPosition SET title_official = :title_official, title_user = :title_user, image_link = :image_link WHERE id = :id")
                ->execute($to_update);

            send_response(200);
            break;
        case 'POST': // Page 19
            $by = $_POST["by"];
            $title_official = $_POST["title_official"];
            $title_user = $_POST["title_non_official"];
            $encoded_image = $_POST["encoded_image"];

            $by_employee = $pdo->query("SELECT code, trusted_status_id FROM Employee WHERE code = $by LIMIT 1")->fetch(PDO::FETCH_ASSOC);
            if ($by_employee === false) {
                send_response(404, "Not Found Self");
            }
            if ($by_employee['trusted_status_id'] < 1) {
                send_response(403, "Forbidden By Status");
            }

            $to_insert = [
                'title_official' => $title_official,
                'title_user' => $title_user,
                'image_link' => null,
            ];

            if (!empty($encoded_image)) {
                $image_link = 'Images/IMAGE_' . time() . '.' . IMAGE_EXTENSION;
                $image = dirname(__FILE__) . '/' . $image_link;
                file_put_contents($image, base64_decode($encoded_image));
                $to_insert['image_link'] = $image_link;
            }

            $pdo->prepare('INSERT INTO InventoryPosition (title_official, title_user, image_link) VALUES (:title_official, :title_user, :image_link)')
                ->execute($to_insert);

            send_response(200);
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