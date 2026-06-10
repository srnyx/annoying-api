package xyz.srnyx.annoyingapi.file.okaeri.migration;

import eu.okaeri.configs.migrate.builtin.NamedMigration;

import static eu.okaeri.configs.migrate.ConfigMigrationDsl.move;
import static eu.okaeri.configs.migrate.ConfigMigrationDsl.multi;


public class A0001_Rename_hyphen_names_to_snake_case extends NamedMigration {
    public A0001_Rename_hyphen_names_to_snake_case() {
        super("renames hyphen-names to snake_case",
                //TODO
                move("api-keys", "api_keys"),

                multi(
                        move("syncing.discord-to-minecraft", "syncing.discord_to_minecraft"),
                        move("syncing.minecraft-to-discord", "syncing.minecraft_to_discord")),

                multi(
                        move("linking.require-link", "linking.require_link"),
                        move("linking.check-on-join", "linking.check_on_join"),
                        move("linking.allow-join-on-failure", "linking.allow_join_on_failure")),

                multi(
                        move("cross-ban", "cross_ban"),
                        move("cross_ban.check-on-join", "cross_ban.check_on_join"),
                        move("cross_ban.allow-join-on-failure", "cross_ban.allow_join_on_failure")),

                multi(
                        move("event-messages", "event_messages"),
                        move("event_messages.detect-ips", "event_messages.detect_ips"),
                        move("event_messages.ignored-types", "event_messages.ignored_types"),
                        move("event_messages.ignored-partner-roles", "event_messages.ignored_partner_roles"),
                        move("event_messages.ignored-formats", "event_messages.ignored_formats"),
                        move("event_messages.host-filter", "event_messages.host_filter")),

                multi(
                        move("advanced.use-testing-api", "advanced.use_testing_api"),
                        move("advanced.websockets.retry-delay", "advanced.websockets.retry_delay")));
    }
}
