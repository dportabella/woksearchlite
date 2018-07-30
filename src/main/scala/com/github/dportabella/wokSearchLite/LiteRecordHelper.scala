package com.github.dportabella.wokSearchLite

class LiteRecordHelper(record: LiteRecord) {
  case class SimpleLabelValuesPair(label: String, values: Seq[String])

  def toSimpleLabelValuesPairs(field: Seq[Option[LabelValuesPair]]): Seq[SimpleLabelValuesPair] =
    field.flatten.map(p => SimpleLabelValuesPair(p.label.get.trim, p.value.flatten.map(_.trim)))

  val fields = List(record.title, record.doctype, record.source, record.authors, record.keywords, record.other)

  val pairs: Map[String, List[String]] =
    fields.flatMap(toSimpleLabelValuesPairs).groupBy(_.label).mapValues(_.flatMap(_.values))

  def values(label: String): List[String] =
    pairs.get(label).toList.flatten

  def value(label: String): Option[String] = {
    val values_ = values(label)
    if (values_.length > 1) println(s"warning: more than one result for LabelValuesPair with label $label: $values_")
    values_.headOption
  }
}
