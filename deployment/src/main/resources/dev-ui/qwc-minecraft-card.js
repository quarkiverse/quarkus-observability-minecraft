import {LitElement, html, css} from 'lit';
import {JsonRpc} from 'jsonrpc';
import {notifier} from 'notifier';
import '@vaadin/button';
import '@vaadin/icon';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset';

export class QwcMinecraftCard extends LitElement {

    jsonRpc = new JsonRpc(this);

    static styles = css`
        .card-content {
            color: var(--lumo-contrast-80pct);
            display: flex;
            flex-direction: column;
            justify-content: flex-start;
            padding: 10px 10px;
            height: 100%;
            font-size: var(--lumo-font-size-s);
            gap: 10px;
        }

        .description {
            padding-bottom: 10px;
            color: var(--lumo-contrast-50pct);
            line-height: 1.5;
        }

    `;

    static properties = {
        extensionName: {attribute: true},
        description: {attribute: true},
        guide: {attribute: true},
        namespace: {attribute: true},
        logoUrl: {attribute: true}
    };

    constructor() {
        super();
    }

    render() {
        return html`<div class="card-content" slot="content">
            <div class="description">
                ${this.logoUrl ? html`<img src="${this.logoUrl}" height="45" style="float: left; margin-right: 10px;" @error="${(e) => e.target.style.display = 'none'}">` : ''}
                ${this.description}
            </div>
            <vaadin-button theme="primary small" @click="${this._setRespawn}">
                <vaadin-icon icon="vaadin:flag" slot="prefix"></vaadin-icon>
                Set A New Spawn Point
            </vaadin-button>
            <vaadin-button theme="primary small" @click="${this._killAndRespawnPlayer}">
                <vaadin-icon icon="vaadin:play" slot="prefix"></vaadin-icon>
                Respawn
            </vaadin-button>
        </div>`;
    }

    _setRespawn() {
        notifier.showInfoMessage('Setting respawn point...');

        this.jsonRpc.setRespawn().then(() => {
            notifier.showSuccessMessage('Respawn point set');
        }).catch(error => {
            notifier.showErrorMessage(error.message || 'Failed to set a new respawn point');
        });
    }

    _killAndRespawnPlayer() {
        notifier.showInfoMessage('Killing player...');
        return this.jsonRpc.killPlayer().then(() => {
            notifier.showInfoMessage('Player killed — respawning...');
            return this.jsonRpc.respawnPlayer();
        }).then(() => {
            notifier.showSuccessMessage('Player respawned at new location');
        }).catch(error => {
            notifier.showErrorMessage(error.message || 'Failed to respawn');
        });
    }
}

customElements.define('qwc-minecraft-card', QwcMinecraftCard);
