import { jsPDF } from 'jspdf'
import type { BusinessProfile, Client, LineItem } from './types'

export interface InvoiceDocument {
  business: BusinessProfile
  client: Client
  invoiceNumber: string
  issueDate: string
  dueDate: string
  items: LineItem[]
  taxRatePercent: number
  notes: string
}

export const formatCurrency = (amount: number): string =>
  new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    maximumFractionDigits: 0,
  }).format(amount)

export const lineItemAmount = (item: LineItem): number => item.quantity * item.unitPrice

export const calculateTotals = (items: LineItem[], taxRatePercent: number) => {
  const subtotal = items.reduce((sum, item) => sum + lineItemAmount(item), 0)
  const tax = subtotal * (taxRatePercent / 100)
  return { subtotal, tax, total: subtotal + tax }
}

export const buildInvoicePdf = (invoice: InvoiceDocument): jsPDF => {
  const doc = new jsPDF({ unit: 'pt', format: 'a4' })
  const marginX = 48
  const pageWidth = doc.internal.pageSize.getWidth()
  let y = 56

  doc.setFont('helvetica', 'bold')
  doc.setFontSize(20)
  doc.text(invoice.business.name, marginX, y)

  doc.setFont('helvetica', 'normal')
  doc.setFontSize(10)
  doc.text('INVOICE', pageWidth - marginX, y, { align: 'right' })

  y += 16
  doc.setFontSize(10)
  doc.text(invoice.business.tagline, marginX, y)
  doc.text(invoice.invoiceNumber, pageWidth - marginX, y, { align: 'right' })

  y += 14
  doc.text(invoice.business.address, marginX, y)
  doc.text(`Issued: ${invoice.issueDate}`, pageWidth - marginX, y, { align: 'right' })

  y += 14
  doc.text(`${invoice.business.email} - ${invoice.business.phone}`, marginX, y)
  doc.text(`Due: ${invoice.dueDate}`, pageWidth - marginX, y, { align: 'right' })

  y += 32
  doc.setFont('helvetica', 'bold')
  doc.text('Bill To', marginX, y)
  y += 16
  doc.setFont('helvetica', 'normal')
  doc.text(invoice.client.name, marginX, y)
  y += 14
  doc.text(`Attn: ${invoice.client.contact}`, marginX, y)
  y += 14
  doc.text(invoice.client.address, marginX, y)
  y += 14
  doc.text(`${invoice.client.email} - Tax ID ${invoice.client.taxId}`, marginX, y)

  y += 30
  const colDesc = marginX
  const colQty = pageWidth - marginX - 260
  const colPrice = pageWidth - marginX - 170
  const colAmount = pageWidth - marginX

  doc.setFont('helvetica', 'bold')
  doc.setFillColor(240, 240, 244)
  doc.rect(marginX, y - 12, pageWidth - marginX * 2, 20, 'F')
  doc.text('Description', colDesc + 4, y + 2)
  doc.text('Qty', colQty, y + 2)
  doc.text('Unit Price', colPrice, y + 2)
  doc.text('Amount', colAmount, y + 2, { align: 'right' })
  y += 22

  doc.setFont('helvetica', 'normal')
  for (const item of invoice.items) {
    doc.text(item.description || '(no description)', colDesc + 4, y)
    doc.text(String(item.quantity), colQty, y)
    doc.text(formatCurrency(item.unitPrice), colPrice, y)
    doc.text(formatCurrency(lineItemAmount(item)), colAmount, y, { align: 'right' })
    y += 20
  }

  y += 6
  doc.setDrawColor(220, 220, 224)
  doc.line(marginX, y, pageWidth - marginX, y)
  y += 18

  const { subtotal, tax, total } = calculateTotals(invoice.items, invoice.taxRatePercent)
  doc.text('Subtotal', colPrice, y)
  doc.text(formatCurrency(subtotal), colAmount, y, { align: 'right' })
  y += 16
  doc.text(`Tax (${invoice.taxRatePercent}%)`, colPrice, y)
  doc.text(formatCurrency(tax), colAmount, y, { align: 'right' })
  y += 18
  doc.setFont('helvetica', 'bold')
  doc.setFontSize(12)
  doc.text('Total', colPrice, y)
  doc.text(formatCurrency(total), colAmount, y, { align: 'right' })

  y += 40
  doc.setFont('helvetica', 'bold')
  doc.setFontSize(10)
  doc.text('Payment details', marginX, y)
  y += 16
  doc.setFont('helvetica', 'normal')
  doc.text(`${invoice.business.bankName} - ${invoice.business.bankAccount}`, marginX, y)
  y += 14
  doc.text(`Account holder: ${invoice.business.bankHolder}`, marginX, y)

  if (invoice.notes.trim()) {
    y += 30
    doc.setFont('helvetica', 'bold')
    doc.text('Notes', marginX, y)
    y += 16
    doc.setFont('helvetica', 'normal')
    const wrapped = doc.splitTextToSize(invoice.notes, pageWidth - marginX * 2)
    doc.text(wrapped, marginX, y)
  }

  return doc
}
