import axios from "axios";

export default async function initHome() {

    const acc = JSON.parse(sessionStorage.getItem("account"));
    if (!acc) {
        window.location.href = "/login";
        return;
    }

    const card = acc.card;

    await loadAccountInfo(card);
    await loadRecords(card);
}

// 获取账号信息
async function loadAccountInfo(card) {
    try {
        const res = await axios.get(`${import.meta.env.VITE_API_URL}/info?card=` + card);
        const data = res.data.data;

        document.getElementById("name").textContent = data.name;
        document.getElementById("card").textContent = data.card;
        document.getElementById("balance").textContent = data.balance;
        document.getElementById("gender").textContent = data.sex || "—";
        document.getElementById("limit").textContent = data.dailyLimit || 0;  // 修复字段名

    } catch (err) {
        alert("账号信息加载失败");
    }
}

// 获取交易记录
async function loadRecords(card) {
    try {
        const res = await axios.get(`${import.meta.env.VITE_API_URL}/transactions?card=` + card);
        const records = res.data.data;

        const list = document.getElementById("recordList");
        list.innerHTML = "";

        if (!records || records.length === 0) {
            list.innerHTML = "<p>没有交易记录</p>";
            return;
        }

        records.forEach(t => {
            const div = document.createElement("div");
            div.className = "record-item";

            div.innerHTML = `
            <div class="left">
                <span class="type">${mapType(t.type)}</span>
                <span class="remark">${t.remark}</span>
            </div>
            <div class="right">
                <span class="amount">${formatAmount(t.type, t.amount)}</span>
                <span class="time">${t.time.replace("T", " ")}</span>
            </div>
            `;

            list.appendChild(div);
        });

    } catch (err) {
        document.getElementById("recordList").innerHTML = "<p>记录加载失败</p>";
    }
}

function mapType(type) {
    switch (type) {
        case "DEPOSIT": return "存款";
        case "WITHDRAW": return "取款";
        case "TRANSFER_OUT": return "转出";
        case "TRANSFER_IN": return "转入";
        default: return type;
    }
}

function formatAmount(type, amount) {
    if (type === "DEPOSIT" || type === "TRANSFER_IN")
        return "+ " + amount;
    return "- " + amount;
}
