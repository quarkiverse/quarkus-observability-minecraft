import {css, html, LitElement} from 'lit';
import {JsonRpc} from 'jsonrpc';
import '@vaadin/button';
import '@vaadin/icon';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset';

export class QwcMinecraftRespawn extends LitElement {

    jsonRpc = new JsonRpc(this);

    static styles = css`
        :host {
            display: flex;
            flex-direction: column;
            gap: 10px;
            height: 100%;
            padding-left: 10px;
            padding-right: 10px;
        }

        .status-message {
            padding: 10px;
            border-radius: 4px;
            background-color: var(--lumo-contrast-5pct);
        }

        .success {
            color: var(--lumo-success-text-color);
            background-color: var(--lumo-success-color-10pct);
        }

        .error {
            color: var(--lumo-error-text-color);
            background-color: var(--lumo-error-color-10pct);
        }
    `;

    static properties = {
        _statusMessage: {state: true},
        _statusType: {state: true},
        _playerDead: {state: true}
    }

    constructor() {
        super();
        this._statusMessage = '';
        this._statusType = '';
        this._playerDead = false;
    }

    render() {
        return html`
            <vaadin-button theme="primary" @click="${this._setRespawn}">
                <vaadin-icon icon="vaadin:flag" slot="prefix"></vaadin-icon>
                Set A New Spawn Point
            </vaadin-button>
            <vaadin-button theme="primary" @click="${this._killAndRespawnPlayer}">
                <vaadin-icon icon="vaadin:play" slot="prefix"></vaadin-icon>
                Respawn
            </vaadin-button>
            ${this._statusMessage ? html`
                <div class="status-message ${this._statusType}">
                    ${this._statusMessage}
                </div>
            ` : ''}
        `;
    }

    _setRespawn() {
        this._statusMessage = 'Setting respawn point...';
        this._statusType = '';

        this.jsonRpc.setRespawn().then(() => {
            this._statusMessage = 'Respawn point set ...';
            this._statusType = 'success';
         }).catch(error => {
            this._statusMessage = `Error: ${error.message || 'Failed to set a new respawn point'}`;
            this._statusType = 'error';
        });
    }

    _killAndRespawnPlayer() {
        this._statusMessage = 'Killing player...';
        this._statusType = '';
        return this.jsonRpc.killPlayer().then(() => {
            this._playerDead = true;
            this._statusMessage = 'Player killed — respawning ...';
            this._statusType = 'success';
            return this.jsonRpc.respawnPlayer()
        }).then(() => {
            this._statusMessage = 'Player respawned at new location';
            this._statusType = 'success';
            this._playerDead = false;
            setTimeout(() => {
                this._statusMessage = '';
                this._statusType = '';
            }, 3000);
        }).catch(error => {
            this._statusMessage = `Error: ${error.message || 'Failed to respawn'}`;
            this._statusType = 'error';
        });
    }
}

customElements.define('qwc-minecraft-respawn', QwcMinecraftRespawn);

// Made with Bob
