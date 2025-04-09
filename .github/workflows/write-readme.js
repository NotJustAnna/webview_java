/*
 * DEVELOPER NOTE:
 * Hello and welcome to one of the weirdest JS scripts I personally have ever written.
 */

const fs = require("node:fs").promises;

/**
 * @typedef {Object} Metadata
 * @property {string} groupId
 * @property {string} artifactId
 * @property {string} latestVersion
 */

const expressions = {
    groupId: /^\s*<groupId>(.+?)<\/groupId>\s*$/,
    artifactId: /^\s*<artifactId>(.+?)<\/artifactId>\s*$/,
    latestVersion: /^\s*<release>(.+?)<\/release>\s*$/,
};

/**
 * @param {Metadata[]} metadata
 * @return {string}
 */
function generateReadme(metadata) {
    const initial = "# Webview Java\n\nPublic maven repository for Webview Java\n\n**Repository URL**: `https://github.com/NotJustAnna/webview_java/raw/maven`\n\n";
    const tableHeader = "| Group ID | Artifact ID | Latest Version |\n| --- | --- | --- |\n";
    const tableRows = metadata.map(m => `| \`${m.groupId}\` | \`${m.artifactId}\` | \`${m.latestVersion}\` |`).join("\n");

    return initial + tableHeader + tableRows;
}

/**
 * @param {string} destination
 * @returns {Promise<void>}
 */
async function main(destination) {
    // find all maven-metadata.xml files in the current directory and subdirectories
    const files = await fs.readdir(".", { withFileTypes: true, recursive: true });
    const metadataFiles = files.filter(it => it.isFile() && it.name === "maven-metadata.xml");

    // read the contents of each maven-metadata.xml file

    /** @type {string[]} */
    const metadataContents = [];

    for (const file of metadataFiles) {
        metadataContents.push(await fs.readFile(`${file.parentPath}/${file.name}`, "utf-8"));
    }

    // parse the contents of each maven-metadata.xml file
    /** @type {Metadata[]} */
    const metadata = metadataContents.map(content => {
        const lines = content.split("\n");
        const metadata = {};

        for (const line of lines) {
            for (const [key, regex] of Object.entries(expressions)) {
                const match = line.match(regex);
                if (match) {
                    metadata[key] = match[1];
                }
            }
        }

        return metadata;
    });

    // generate the README.md file
    await fs.writeFile(destination, generateReadme(metadata), "utf-8");
}

module.exports = { main };